package com.tinkisqa.iam.application.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.iam.application.config.IamProperties;
import com.tinkisqa.iam.application.dto.AuthDtos;
import com.tinkisqa.iam.domain.exception.DomainException;
import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.LoginAttempt;
import com.tinkisqa.iam.domain.model.OtpChallenge;
import com.tinkisqa.iam.domain.model.Permission;
import com.tinkisqa.iam.domain.model.RefreshToken;
import com.tinkisqa.iam.domain.model.SecurityEvent;
import com.tinkisqa.iam.domain.model.SecurityEventType;
import com.tinkisqa.iam.domain.port.AccountRepository;
import com.tinkisqa.iam.domain.port.DomainEventPublisher;
import com.tinkisqa.iam.domain.port.LoginAttemptRepository;
import com.tinkisqa.iam.domain.port.NotificationSender;
import com.tinkisqa.iam.domain.port.OtpChallengeRepository;
import com.tinkisqa.iam.domain.port.OtpGenerator;
import com.tinkisqa.iam.domain.port.PasswordHasher;
import com.tinkisqa.iam.domain.port.RefreshTokenRepository;
import com.tinkisqa.iam.domain.port.SecurityEventRepository;
import com.tinkisqa.iam.domain.port.TokenIssuer;
import com.tinkisqa.iam.domain.service.AccountRules;
import com.tinkisqa.iam.domain.service.PermissionRules;

/**
 * Casos de uso de autenticación: registro, verificación de correo, inicio de
 * sesión (con bloqueo por intentos), refresco de token y cierre de sesión.
 */
@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AccountRepository accountRepository;
    private final OtpChallengeRepository otpChallengeRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecurityEventRepository securityEventRepository;
    private final LoginAttemptRepository loginAttemptRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;
    private final OtpGenerator otpGenerator;
    private final NotificationSender notificationSender;
    private final DomainEventPublisher eventPublisher;
    private final IamProperties properties;
    private final java.time.Clock clock;

    public AuthService(
            AccountRepository accountRepository,
            OtpChallengeRepository otpChallengeRepository,
            RefreshTokenRepository refreshTokenRepository,
            SecurityEventRepository securityEventRepository,
            LoginAttemptRepository loginAttemptRepository,
            PasswordHasher passwordHasher,
            TokenIssuer tokenIssuer,
            OtpGenerator otpGenerator,
            NotificationSender notificationSender,
            DomainEventPublisher eventPublisher,
            IamProperties properties,
            java.time.Clock clock) {
        this.accountRepository = accountRepository;
        this.otpChallengeRepository = otpChallengeRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityEventRepository = securityEventRepository;
        this.loginAttemptRepository = loginAttemptRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
        this.otpGenerator = otpGenerator;
        this.notificationSender = notificationSender;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public AuthDtos.RegisterResponse register(AuthDtos.RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (!AccountRules.isEmailValid(email)) {
            throw DomainException.validation("El correo electrónico no es válido.");
        }
        if (!AccountRules.isNombreCompletoValid(request.nombreCompleto())) {
            throw DomainException.validation("Ingresa nombres y apellidos (mínimo 3 caracteres cada uno).");
        }
        if (!AccountRules.passwordStrength(request.password()).acceptable()) {
            throw DomainException.validation(
                    "La contraseña debe tener al menos 10 caracteres y combinar mayúsculas, minúsculas, números o símbolos.");
        }
        if (!request.acceptTerms()) {
            throw DomainException.validation("Debes aceptar los términos y condiciones.");
        }
        if (accountRepository.existsByEmail(email)) {
            throw DomainException.conflict("Ya existe una cuenta registrada con ese correo.");
        }

        Instant now = clock.instant();
        UUID id = UUID.randomUUID();
        Account account = Account.registered(
                id, email, passwordHasher.hash(request.password()), request.nombreCompleto().trim(), now);
        accountRepository.save(account);

        securityEventRepository.save(SecurityEvent.of(id, SecurityEventType.REGISTRO, "Registro de cuenta", now));
        eventPublisher.publish("iam.usuario.registrado", Map.of("accountId", id.toString(), "email", email));

        return sendEmailOtp(account);
    }

    @Transactional
    public AuthDtos.AuthResponse verifyEmail(AuthDtos.VerifyEmailRequest request) {
        String email = normalizeEmail(request.email());
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> DomainException.notFound("No existe una cuenta con ese correo."));
        Instant now = clock.instant();
        OtpChallenge challenge = otpChallengeRepository
                .findLatestActive(email, OtpChallenge.Channel.EMAIL, OtpChallenge.Purpose.EMAIL_VERIFICATION)
                .orElseThrow(() -> DomainException.validation("No hay un código vigente. Solicita uno nuevo."));

        validateAndConsume(challenge, request.code(), now);

        Account verified = account.withEmailVerified(now);
        accountRepository.save(verified);
        securityEventRepository.save(
                SecurityEvent.of(verified.id(), SecurityEventType.CORREO_VERIFICADO, "Correo verificado", now));
        eventPublisher.publish("iam.correo.verificado", Map.of("accountId", verified.id().toString()));

        return issue(verified);
    }

    @Transactional
    public AuthDtos.RegisterResponse resendVerification(AuthDtos.ResendOtpRequest request) {
        String email = normalizeEmail(request.email());
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> DomainException.notFound("No existe una cuenta con ese correo."));
        if (account.emailVerified()) {
            throw DomainException.businessRule("El correo ya está verificado.");
        }

        Instant now = clock.instant();
        Duration cooldown = properties.getIam().getOtp().getResendCooldown();
        Optional<OtpChallenge> current = otpChallengeRepository
                .findLatestActive(email, OtpChallenge.Channel.EMAIL, OtpChallenge.Purpose.EMAIL_VERIFICATION);
        if (current.isPresent()) {
            Duration elapsed = Duration.between(current.get().createdAt(), now);
            if (elapsed.compareTo(cooldown) < 0) {
                long remaining = Math.max(1, cooldown.minus(elapsed).getSeconds());
                throw DomainException.tooManyRequests(
                        "Espera " + remaining + " segundo(s) antes de solicitar otro código.");
            }
        }

        securityEventRepository.save(SecurityEvent.of(
                account.id(), SecurityEventType.OTP_REENVIADO, "Reenvío de código de verificación", now));
        return sendEmailOtp(account);
    }

    @Transactional
    public AuthDtos.AuthResponse login(AuthDtos.LoginRequest request) {
        String email = normalizeEmail(request.email());
        Instant now = clock.instant();
        LoginAttempt attempt = loginAttemptRepository.findByEmail(email)
                .orElse(LoginAttempt.empty(email, now));

        if (attempt.isLocked(now)) {
            long minutes = Math.max(1, attempt.remainingLock(now).toMinutes());
            throw DomainException.tooManyRequests(
                    "Demasiados intentos fallidos. La cuenta está bloqueada por " + minutes + " minuto(s).");
        }

        Optional<Account> maybeAccount = accountRepository.findByEmail(email);
        boolean credentialsOk = maybeAccount.isPresent()
                && passwordHasher.matches(request.password(), maybeAccount.get().passwordHash());

        if (!credentialsOk) {
            LoginAttempt updated = loginAttemptRepository.save(attempt.registerFailure(now));
            UUID accountId = maybeAccount.map(Account::id).orElse(null);
            securityEventRepository.save(SecurityEvent.of(
                    accountId, SecurityEventType.LOGIN_FALLIDO, "Intento fallido para " + email, now));
            if (updated.isLocked(now)) {
                securityEventRepository.save(SecurityEvent.of(
                        accountId, SecurityEventType.CUENTA_BLOQUEADA, "Bloqueo por intentos fallidos", now));
                throw DomainException.tooManyRequests(
                        "Demasiados intentos fallidos. La cuenta se bloqueó por 15 minutos.");
            }
            throw DomainException.unauthorized("Correo o contraseña incorrectos.");
        }

        Account account = maybeAccount.get();
        if (!account.emailVerified()) {
            throw DomainException.forbidden("Debes verificar tu correo electrónico antes de iniciar sesión.");
        }
        if (!account.isLoginAllowed()) {
            throw DomainException.forbidden("La cuenta está suspendida. Contacta al equipo.");
        }

        loginAttemptRepository.save(attempt.reset(now));
        securityEventRepository.save(SecurityEvent.of(
                account.id(), SecurityEventType.LOGIN_EXITOSO, "Inicio de sesión correcto", now));
        return issue(account);
    }

    @Transactional
    public AuthDtos.AuthResponse refresh(AuthDtos.RefreshRequest request) {
        TokenIssuer.ParsedToken parsed = tokenIssuer.parse(request.refreshToken());
        if (!"refresh".equals(parsed.type())) {
            throw DomainException.unauthorized("El token no es un token de refresco.");
        }
        Instant now = clock.instant();
        RefreshToken stored = refreshTokenRepository.findByJti(parseJti(parsed))
                .orElseThrow(() -> DomainException.unauthorized("La sesión ya no es válida."));
        if (!stored.isActive(now) || !tokenIssuer.hashToken(request.refreshToken()).equals(stored.tokenHash())) {
            throw DomainException.unauthorized("La sesión expiró o fue revocada. Inicia sesión de nuevo.");
        }
        Account account = accountRepository.findById(stored.accountId())
                .orElseThrow(() -> DomainException.notFound("Cuenta no encontrada."));

        // Rotación: se revoca el token usado antes de emitir uno nuevo.
        refreshTokenRepository.save(new RefreshToken(
                stored.jti(), stored.accountId(), stored.tokenHash(), stored.expiresAt(), now, stored.createdAt()));
        return issue(account);
    }

    @Transactional
    public void logout(AuthDtos.LogoutRequest request) {
        try {
            TokenIssuer.ParsedToken parsed = tokenIssuer.parse(request.refreshToken());
            Instant now = clock.instant();
            refreshTokenRepository.findByJti(parseJti(parsed)).ifPresent(token -> {
                refreshTokenRepository.save(new RefreshToken(
                        token.jti(), token.accountId(), token.tokenHash(),
                        token.expiresAt(), now, token.createdAt()));
                securityEventRepository.save(SecurityEvent.of(
                        token.accountId(), SecurityEventType.LOGOUT, "Cierre de sesión", now));
            });
        } catch (RuntimeException ex) {
            log.debug("Cierre de sesión con token inválido: {}", ex.getMessage());
        }
    }

    private AuthDtos.RegisterResponse sendEmailOtp(Account account) {
        Instant now = clock.instant();
        otpChallengeRepository.invalidateAll(
                account.email(), OtpChallenge.Channel.EMAIL, OtpChallenge.Purpose.EMAIL_VERIFICATION);

        String code = otpGenerator.generateCode();
        Duration ttl = properties.getIam().getOtp().getTtl();
        otpChallengeRepository.save(new OtpChallenge(
                UUID.randomUUID(), account.email(), OtpChallenge.Channel.EMAIL,
                OtpChallenge.Purpose.EMAIL_VERIFICATION, otpGenerator.hash(code), 0, now.plus(ttl), null, now));
        notificationSender.sendEmailCode(account.email(), code, "VERIFICACION_CORREO");

        return new AuthDtos.RegisterResponse(
                account.email(),
                "Te enviamos un código de 6 dígitos para verificar tu correo.",
                ttl.getSeconds(),
                properties.getIam().getOtp().getResendCooldown().getSeconds());
    }

    private void validateAndConsume(OtpChallenge challenge, String code, Instant now) {
        if (challenge.isConsumed()) {
            throw DomainException.validation("El código ya fue utilizado.");
        }
        if (challenge.isExpired(now)) {
            throw DomainException.validation("El código expiró. Solicita uno nuevo.");
        }
        if (challenge.attempts() >= properties.getIam().getOtp().getMaxAttempts()) {
            throw DomainException.tooManyRequests("Demasiados intentos. Solicita un nuevo código.");
        }
        if (!otpGenerator.matches(code, challenge.codeHash())) {
            otpChallengeRepository.save(challenge.withAttempt());
            throw DomainException.validation("El código no es correcto.");
        }
        otpChallengeRepository.save(challenge.consumed(now));
    }

    AuthDtos.AuthResponse issue(Account account) {
        Instant now = clock.instant();
        Set<Permission> permissions = PermissionRules.permissionsFor(account.identityStatus(), account.accessStatus());
        TokenIssuer.IssuedTokens tokens = tokenIssuer.issueTokens(account, permissions);

        refreshTokenRepository.save(new RefreshToken(
                UUID.fromString(tokens.refreshJti()),
                account.id(),
                tokenIssuer.hashToken(tokens.refreshToken()),
                now.plus(properties.getIam().getRefreshTokenTtl()),
                null,
                now));

        List<String> permissionNames = tokenIssuer.serializePermissions(permissions);
        AuthDtos.UserSummary summary = new AuthDtos.UserSummary(
                account.id(),
                account.email(),
                account.nombreCompleto(),
                account.role().name(),
                account.identityStatus().name(),
                account.accessStatus().name(),
                account.emailVerified(),
                permissionNames);

        return new AuthDtos.AuthResponse(
                tokens.accessToken(), tokens.refreshToken(), "Bearer", tokens.expiresInSeconds(), summary);
    }

    private static UUID parseJti(TokenIssuer.ParsedToken parsed) {
        try {
            return UUID.fromString(parsed.jti());
        } catch (RuntimeException ex) {
            throw DomainException.unauthorized("El token no contiene un identificador de sesión válido.");
        }
    }

    private static String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }
}
