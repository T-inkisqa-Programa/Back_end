package com.tinkisqa.iam.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Token de refresco persistido (solo su hash). Permite revocar sesiones sin
 * guardar el token en claro.
 */
public record RefreshToken(
        UUID jti,
        UUID accountId,
        String tokenHash,
        Instant expiresAt,
        Instant revokedAt,
        Instant createdAt) {

    public boolean isActive(Instant now) {
        return revokedAt == null && now.isBefore(expiresAt);
    }

    public RefreshToken revoked(Instant now) {
        return new RefreshToken(jti, accountId, tokenHash, expiresAt, now, createdAt);
    }
}
