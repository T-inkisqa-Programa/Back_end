package com.tinkisqa.iam.infrastructure.seed;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.model.AccessStatus;
import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.GenderIdentity;
import com.tinkisqa.iam.domain.model.IdentityStatus;
import com.tinkisqa.iam.domain.model.Role;
import com.tinkisqa.iam.domain.model.Verification;
import com.tinkisqa.iam.domain.model.VerificationState;
import com.tinkisqa.iam.domain.port.AccountRepository;
import com.tinkisqa.iam.domain.port.OtpGenerator;
import com.tinkisqa.iam.domain.port.PasswordHasher;
import com.tinkisqa.iam.domain.port.VerificationRepository;

/**
 * Crea las tres cuentas de prueba (ADMIN, Usuaria A y Usuaria B).
 *
 * <p>Solo se ejecuta si {@code tinkisqa.iam.seed.enabled=true} (por ejemplo
 * desde {@code docker-compose.yml}); está desactivado por defecto y nunca debe
 * activarse en producción. Es idempotente: si el correo ya existe, no hace
 * nada. Las cuentas quedan con identidad verificada y acceso aprobado, y con
 * una verificación documental registrada como resuelta en desarrollo.</p>
 */
@Component
@ConditionalOnProperty(prefix = "tinkisqa.iam.seed", name = "enabled", havingValue = "true")
public class TestAccountSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(TestAccountSeeder.class);
    private static final String PROVIDER = "seed-dev";

    private record SeedAccount(String email, String password, String nombre, Role role, String dni, String telefono) {
    }

    private static final List<SeedAccount> ACCOUNTS = List.of(
            new SeedAccount("admin@tinkisqa.dev", "AdminSegura123!", "Admin T'inkisqa",
                    Role.ROLE_ADMIN, "10000001", "900000001"),
            new SeedAccount("usuaria.a@tinkisqa.dev", "UsuariaSegura123!", "Usuaria Alfa",
                    Role.ROLE_USER, "10000002", "900000002"),
            new SeedAccount("usuaria.b@tinkisqa.dev", "UsuariaSegura123!", "Usuaria Beta",
                    Role.ROLE_USER, "10000003", "900000003"));

    private final AccountRepository accountRepository;
    private final VerificationRepository verificationRepository;
    private final PasswordHasher passwordHasher;
    private final OtpGenerator otpGenerator;
    private final java.time.Clock clock;

    public TestAccountSeeder(
            AccountRepository accountRepository,
            VerificationRepository verificationRepository,
            PasswordHasher passwordHasher,
            OtpGenerator otpGenerator,
            java.time.Clock clock) {
        this.accountRepository = accountRepository;
        this.verificationRepository = verificationRepository;
        this.passwordHasher = passwordHasher;
        this.otpGenerator = otpGenerator;
        this.clock = clock;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Sembrando cuentas de prueba (tinkisqa.iam.seed.enabled=true)...");
        for (SeedAccount seed : ACCOUNTS) {
            try {
                seed(seed);
            } catch (RuntimeException ex) {
                log.warn("No se pudo sembrar la cuenta {}: {}", seed.email(), ex.getMessage());
            }
        }
    }

    private void seed(SeedAccount seed) {
        if (accountRepository.existsByEmail(seed.email())) {
            log.info("La cuenta de prueba {} ya existe; se omite.", seed.email());
            return;
        }

        Instant now = clock.instant();
        UUID accountId = UUID.randomUUID();
        Account account = Account.registered(
                        accountId, seed.email(), passwordHasher.hash(seed.password()), seed.nombre(), now)
                .withEmailVerified(now)
                .withNormasAceptadas(true, now)
                .withProfile(seed.nombre(), "Cuenta de prueba de desarrollo.", GenderIdentity.MUJER, null,
                        Set.of("bienestar", "comunidad"), null, now)
                .withIdentityStatus(IdentityStatus.IDENTIDAD_VERIFICADA, now)
                .withAccessStatus(AccessStatus.ACCESO_APROBADO, now)
                .withRole(seed.role(), now);
        accountRepository.save(account);

        String fingerprint = otpGenerator.fingerprintDocument(seed.dni());
        if (verificationRepository.existsByDniFingerprint(fingerprint)) {
            log.warn("El documento sembrado {} ya está vinculado a otra cuenta; {} queda sin verificación.",
                    seed.dni(), seed.email());
        } else {
            verificationRepository.save(new Verification(
                    UUID.randomUUID(), accountId, fingerprint, seed.telefono(),
                    VerificationState.VERIFICADA, PROVIDER, now, now,
                    "Cuenta de prueba: verificación resuelta en desarrollo."));
        }

        log.info("Cuenta de prueba creada: {} ({})", seed.email(), seed.role().name());
    }
}
