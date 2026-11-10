package com.tinkisqa.iam.application.service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.iam.application.config.IamProperties;
import com.tinkisqa.iam.application.dto.VerificationDtos;
import com.tinkisqa.iam.domain.exception.DomainException;
import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.IdentityStatus;
import com.tinkisqa.iam.domain.model.OtpChallenge;
import com.tinkisqa.iam.domain.model.SecurityEvent;
import com.tinkisqa.iam.domain.model.SecurityEventType;
import com.tinkisqa.iam.domain.model.Verification;
import com.tinkisqa.iam.domain.model.VerificationState;
import com.tinkisqa.iam.domain.port.AccountRepository;
import com.tinkisqa.iam.domain.port.DomainEventPublisher;
import com.tinkisqa.iam.domain.port.IdentityVerificationPort;
import com.tinkisqa.iam.domain.port.NotificationSender;
import com.tinkisqa.iam.domain.port.OtpChallengeRepository;
import com.tinkisqa.iam.domain.port.OtpGenerator;
import com.tinkisqa.iam.domain.port.SecurityEventRepository;
import com.tinkisqa.iam.domain.port.VerificationRepository;
import com.tinkisqa.iam.domain.service.AccountRules;

/**
 * Casos de uso de verificación de identidad (DNI) y de teléfono.
 *
 * <p>La verificación documental NUNCA se aprueba automáticamente: se envía a
 * revisión humana y solo el equipo de moderación puede resolverla.</p>
 */
@Service
public class VerificationService {

    private final AccountRepository accountRepository;
    private final VerificationRepository verificationRepository;
    private final SecurityEventRepository securityEventRepository;
    private final OtpChallengeRepository otpChallengeRepository;
    private final IdentityVerificationPort identityVerificationPort;
    private final OtpGenerator otpGenerator;
    private final NotificationSender notificationSender;
    private final DomainEventPublisher eventPublisher;
    private final AdmissionService admissionService;
    private final IamProperties properties;
    private final java.time.Clock clock;

    public VerificationService(
            AccountRepository accountRepository,
            VerificationRepository verificationRepository,
            SecurityEventRepository securityEventRepository,
            OtpChallengeRepository otpChallengeRepository,
            IdentityVerificationPort identityVerificationPort,
            OtpGenerator otpGenerator,
            NotificationSender notificationSender,
            DomainEventPublisher eventPublisher,
            AdmissionService admissionService,
            IamProperties properties,
            java.time.Clock clock) {
        this.accountRepository = accountRepository;
        this.verificationRepository = verificationRepository;
        this.securityEventRepository = securityEventRepository;
        this.otpChallengeRepository = otpChallengeRepository;
        this.identityVerificationPort = identityVerificationPort;
        this.otpGenerator = otpGenerator;
        this.notificationSender = notificationSender;
        this.eventPublisher = eventPublisher;
        this.admissionService = admissionService;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public VerificationDtos.VerificationResponse submit(UUID accountId, VerificationDtos.SubmitVerificationRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> DomainException.notFound("Cuenta no encontrada."));
        Instant now = clock.instant();
        String dni = request.dni().trim();
        String telefono = request.telefono().trim();

        if (!AccountRules.isDniValid(dni)) {
            throw DomainException.validation("El DNI debe tener exactamente 8 dígitos.");
        }
        if (!AccountRules.isPhoneValid(telefono)) {
            throw DomainException.validation("El teléfono debe tener exactamente 9 dígitos.");
        }

        String fingerprint = otpGenerator.fingerprintDocument(dni);
        if (verificationRepository.existsByDniFingerprint(fingerprint)) {
            securityEventRepository.save(SecurityEvent.of(accountId, SecurityEventType.DOCUMENTO_DUPLICADO,
                    "Intento de registrar un documento ya vinculado a otra cuenta", now));
            throw DomainException.conflict(
                    "Este documento ya está vinculado a otra cuenta. Contacta al equipo si crees que es un error.");
        }

        IdentityVerificationPort.Result result =
                identityVerificationPort.verify(dni, telefono, request.selfieReference());
        VerificationState state = result.outcome() == IdentityVerificationPort.Outcome.REVISION_HUMANA
                ? VerificationState.REVISION_HUMANA
                : VerificationState.RECHAZADA;

        Verification verification = new Verification(
                UUID.randomUUID(), account.id(), fingerprint, telefono, state,
                result.provider(), now, null, result.reason());
        verificationRepository.save(verification);

        securityEventRepository.save(SecurityEvent.of(accountId, SecurityEventType.VERIFICACION_ENVIADA,
                "Verificación enviada; pendiente de revisión humana", now));
        eventPublisher.publish("iam.verificacion.enviada",
                java.util.Map.of("accountId", accountId.toString(), "state", state.name()));

        return toResponse(verification);
    }

    @Transactional(readOnly = true)
    public VerificationDtos.VerificationResponse mine(UUID accountId) {
        return verificationRepository.findLatestByAccountId(accountId)
                .map(VerificationService::toResponse)
                .orElseThrow(() -> DomainException.notFound("Aún no has enviado una verificación de identidad."));
    }

    @Transactional
    public VerificationDtos.VerificationResponse review(
            UUID reviewerId, UUID verificationId, VerificationDtos.ReviewVerificationRequest request) {
        Verification verification = verificationRepository.findById(verificationId)
                .orElseThrow(() -> DomainException.notFound("Verificación no encontrada."));
        if (!verification.isOpen()) {
            throw DomainException.businessRule("Esta verificación ya fue resuelta.");
        }

        Instant now = clock.instant();
        VerificationState newState = VerificationState.valueOf(request.decision());
        Verification resolved = verification.resolved(newState, request.notes(), now);
        verificationRepository.save(resolved);

        Account account = accountRepository.findById(verification.accountId())
                .orElseThrow(() -> DomainException.notFound("Cuenta no encontrada."));
        IdentityStatus identityStatus = newState == VerificationState.VERIFICADA
                ? IdentityStatus.IDENTIDAD_VERIFICADA
                : IdentityStatus.VERIFICACION_RECHAZADA;

        Account updated = admissionService.recompute(account.withIdentityStatus(identityStatus, now), now);
        accountRepository.save(updated);

        securityEventRepository.save(SecurityEvent.of(account.id(), SecurityEventType.VERIFICACION_RESUELTA,
                "Verificación resuelta como " + newState.name() + " por " + reviewerId, now));
        eventPublisher.publish("iam.verificacion.resuelta",
                java.util.Map.of("accountId", account.id().toString(), "state", newState.name()));

        return toResponse(resolved);
    }

    @Transactional
    public VerificationDtos.OtpSentResponse sendPhoneOtp(UUID accountId, VerificationDtos.SendPhoneOtpRequest request) {
        accountRepository.findById(accountId)
                .orElseThrow(() -> DomainException.notFound("Cuenta no encontrada."));
        Instant now = clock.instant();
        String phone = request.telefono().trim();
        if (!AccountRules.isPhoneValid(phone)) {
            throw DomainException.validation("El teléfono debe tener exactamente 9 dígitos.");
        }

        otpChallengeRepository.invalidateAll(phone, OtpChallenge.Channel.SMS, OtpChallenge.Purpose.PHONE_VERIFICATION);
        String code = otpGenerator.generateCode();
        Duration ttl = properties.getIam().getOtp().getTtl();
        otpChallengeRepository.save(new OtpChallenge(
                UUID.randomUUID(), phone, OtpChallenge.Channel.SMS,
                OtpChallenge.Purpose.PHONE_VERIFICATION, otpGenerator.hash(code), 0, now.plus(ttl), null, now));
        notificationSender.sendSmsCode(phone, code, "VERIFICACION_TELEFONO");

        return new VerificationDtos.OtpSentResponse(
                phone, "SMS", ttl.getSeconds(), properties.getIam().getOtp().getResendCooldown().getSeconds());
    }

    @Transactional
    public void verifyPhoneOtp(UUID accountId, VerificationDtos.VerifyPhoneOtpRequest request) {
        Instant now = clock.instant();
        String phone = request.telefono().trim();
        OtpChallenge challenge = otpChallengeRepository
                .findLatestActive(phone, OtpChallenge.Channel.SMS, OtpChallenge.Purpose.PHONE_VERIFICATION)
                .orElseThrow(() -> DomainException.validation("No hay un código vigente para ese teléfono."));

        if (challenge.isConsumed()) {
            throw DomainException.validation("El código ya fue utilizado.");
        }
        if (challenge.isExpired(now)) {
            throw DomainException.validation("El código expiró. Solicita uno nuevo.");
        }
        if (challenge.attempts() >= properties.getIam().getOtp().getMaxAttempts()) {
            throw DomainException.tooManyRequests("Demasiados intentos. Solicita un nuevo código.");
        }
        if (!otpGenerator.matches(request.code(), challenge.codeHash())) {
            otpChallengeRepository.save(challenge.withAttempt());
            throw DomainException.validation("El código no es correcto.");
        }
        otpChallengeRepository.save(challenge.consumed(now));

        securityEventRepository.save(SecurityEvent.of(accountId, SecurityEventType.TELEFONO_VERIFICADO,
                "Teléfono verificado: " + phone, now));
    }

    private static VerificationDtos.VerificationResponse toResponse(Verification verification) {
        return new VerificationDtos.VerificationResponse(
                verification.id(),
                verification.state().name(),
                verification.provider(),
                verification.submittedAt(),
                verification.resolvedAt(),
                verification.notes(),
                verification.isOpen());
    }
}
