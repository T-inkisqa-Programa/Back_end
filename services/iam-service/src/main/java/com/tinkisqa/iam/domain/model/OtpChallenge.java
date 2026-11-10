package com.tinkisqa.iam.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Desafío OTP (código de un solo uso) para verificar correo o teléfono.
 *
 * <p>El código se guarda hasheado, con caducidad (10 minutos), máximo de
 * intentos (5) y tiempos de reenvío. Se usa el mismo agregado para correo y SMS
 * cambiando el {@link Channel}.</p>
 */
public record OtpChallenge(
        UUID id,
        String target,
        Channel channel,
        Purpose purpose,
        String codeHash,
        int attempts,
        Instant expiresAt,
        Instant consumedAt,
        Instant createdAt) {

    public enum Channel {
        EMAIL,
        SMS
    }

    public enum Purpose {
        EMAIL_VERIFICATION,
        PHONE_VERIFICATION,
        PASSWORD_RESET
    }

    public boolean isExpired(Instant now) {
        return now.isAfter(expiresAt);
    }

    public boolean isConsumed() {
        return consumedAt != null;
    }

    public OtpChallenge withAttempt() {
        return new OtpChallenge(id, target, channel, purpose, codeHash, attempts + 1,
                expiresAt, consumedAt, createdAt);
    }

    public OtpChallenge consumed(Instant now) {
        return new OtpChallenge(id, target, channel, purpose, codeHash, attempts, expiresAt, now, createdAt);
    }
}
