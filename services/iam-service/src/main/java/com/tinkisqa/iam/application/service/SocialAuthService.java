package com.tinkisqa.iam.application.service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.iam.application.dto.AuthDtos;
import com.tinkisqa.iam.application.service.social.FacebookTokenVerifier;
import com.tinkisqa.iam.application.service.social.GoogleTokenVerifier;
import com.tinkisqa.iam.domain.exception.DomainException;
import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.SecurityEvent;
import com.tinkisqa.iam.domain.model.SecurityEventType;
import com.tinkisqa.iam.domain.port.AccountRepository;
import com.tinkisqa.iam.domain.port.DomainEventPublisher;
import com.tinkisqa.iam.domain.port.PasswordHasher;
import com.tinkisqa.iam.domain.port.SecurityEventRepository;

/**
 * Login social (Google / Facebook).
 *
 * <p>Valida el token entregado por el proveedor OAuth, crea la cuenta si es la
 * primera vez que el usuario ingresa y emite los tokens JWT de T'inkisqa
 * reutilizando la misma emisión de {@link AuthService}.</p>
 */
@Service
public class SocialAuthService {

    private final AuthService authService;
    private final AccountRepository accountRepository;
    private final PasswordHasher passwordHasher;
    private final SecurityEventRepository securityEventRepository;
    private final DomainEventPublisher eventPublisher;
    private final Clock clock;
    private final GoogleTokenVerifier googleVerifier;
    private final FacebookTokenVerifier facebookVerifier;

    public SocialAuthService(AuthService authService,
                             AccountRepository accountRepository,
                             PasswordHasher passwordHasher,
                             SecurityEventRepository securityEventRepository,
                             DomainEventPublisher eventPublisher,
                             Clock clock,
                             GoogleTokenVerifier googleVerifier,
                             FacebookTokenVerifier facebookVerifier) {
        this.authService = authService;
        this.accountRepository = accountRepository;
        this.passwordHasher = passwordHasher;
        this.securityEventRepository = securityEventRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
        this.googleVerifier = googleVerifier;
        this.facebookVerifier = facebookVerifier;
    }

    @Transactional
    public AuthDtos.AuthResponse loginOrRegister(String provider, String idToken, String accessToken) {
        String email;
        String fullName;
        try {
            switch (provider == null ? "" : provider.toLowerCase()) {
                case "google" -> {
                    var payload = googleVerifier.verifyIdToken(idToken);
                    email = payload.email();
                    fullName = payload.name();
                }
                case "facebook" -> {
                    var payload = facebookVerifier.verifyAccessToken(accessToken);
                    email = payload.email();
                    fullName = payload.name();
                }
                default -> throw DomainException.validation("Proveedor social no soportado: " + provider);
            }
        } catch (IllegalArgumentException e) {
            throw DomainException.validation(e.getMessage());
        }

        email = email.trim().toLowerCase(java.util.Locale.ROOT);
        Instant now = clock.instant();
        Account account = accountRepository.findByEmail(email).orElse(null);
        if (account == null) {
            UUID id = UUID.randomUUID();
            // Contraseña aleatoria: el usuario nunca inicia sesión con ella
            // (solo vía OAuth), pero la cuenta necesita un hash válido.
            String randomPwd = UUID.randomUUID().toString();
            String nombre = fullName != null && !fullName.isBlank() ? fullName.trim() : email;
            account = Account.registered(id, email, passwordHasher.hash(randomPwd), nombre, now);
            accountRepository.save(account);
            securityEventRepository.save(
                    SecurityEvent.of(id, SecurityEventType.REGISTRO, "Registro via " + provider, now));
            eventPublisher.publish("iam.usuario.registrado",
                    Map.of("accountId", id.toString(), "email", email));
        }

        AuthDtos.AuthResponse response = authService.issue(account);
        securityEventRepository.save(SecurityEvent.of(
                account.id(), SecurityEventType.LOGIN_EXITOSO,
                "Inicio de sesión social (" + provider + ")", clock.instant()));
        return response;
    }
}
