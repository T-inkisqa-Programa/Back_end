package com.tinkisqa.iam.domain.port;

import java.util.Optional;
import java.util.UUID;

import com.tinkisqa.iam.domain.model.RefreshToken;

/** Puerto de salida: persistencia y revocación de tokens de refresco. */
public interface RefreshTokenRepository {

    RefreshToken save(RefreshToken token);

    Optional<RefreshToken> findByJti(UUID jti);

    void revokeAllForAccount(UUID accountId, java.time.Instant now);
}
