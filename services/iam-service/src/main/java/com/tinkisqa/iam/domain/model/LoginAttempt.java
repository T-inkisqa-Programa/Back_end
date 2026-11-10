package com.tinkisqa.iam.domain.model;

import java.time.Duration;
import java.time.Instant;

/**
 * Control de intentos de inicio de sesión por correo.
 *
 * <p>Regla de negocio (espejo del frontend): tras 5 intentos fallidos la cuenta
 * se bloquea durante 15 minutos.</p>
 */
public record LoginAttempt(
        String email,
        int attempts,
        Instant lockedUntil,
        Instant updatedAt) {

    public static final int MAX_ATTEMPTS = 5;
    public static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    public static LoginAttempt empty(String email, Instant now) {
        return new LoginAttempt(email, 0, null, now);
    }

    public boolean isLocked(Instant now) {
        return lockedUntil != null && now.isBefore(lockedUntil);
    }

    public Duration remainingLock(Instant now) {
        if (!isLocked(now)) {
            return Duration.ZERO;
        }
        return Duration.between(now, lockedUntil);
    }

    /** Registra un intento fallido y bloquea si se alcanza el máximo. */
    public LoginAttempt registerFailure(Instant now) {
        int next = attempts + 1;
        Instant lock = next >= MAX_ATTEMPTS ? now.plus(LOCK_DURATION) : null;
        return new LoginAttempt(email, next, lock, now);
    }

    /** Reinicia el contador tras un inicio de sesión correcto. */
    public LoginAttempt reset(Instant now) {
        return new LoginAttempt(email, 0, null, now);
    }
}
