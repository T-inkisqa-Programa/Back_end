package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.model.RefreshToken;
import com.tinkisqa.iam.domain.port.RefreshTokenRepository;

/** Adaptador que implementa {@link RefreshTokenRepository} sobre JPA. */
@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepository {

    private final RefreshTokenJpaRepository repository;

    public RefreshTokenRepositoryAdapter(RefreshTokenJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public RefreshToken save(RefreshToken token) {
        return toDomain(repository.save(toEntity(token)));
    }

    @Override
    public Optional<RefreshToken> findByJti(UUID jti) {
        return repository.findById(jti).map(RefreshTokenRepositoryAdapter::toDomain);
    }

    @Override
    public void revokeAllForAccount(UUID accountId, Instant now) {
        List<RefreshTokenJpaEntity> active = repository.findByAccountIdAndRevokedAtIsNull(accountId);
        active.forEach(entity -> entity.setRevokedAt(now));
        repository.saveAll(active);
    }

    private static RefreshTokenJpaEntity toEntity(RefreshToken token) {
        RefreshTokenJpaEntity entity = new RefreshTokenJpaEntity();
        entity.setJti(token.jti());
        entity.setAccountId(token.accountId());
        entity.setTokenHash(token.tokenHash());
        entity.setExpiresAt(token.expiresAt());
        entity.setRevokedAt(token.revokedAt());
        entity.setCreatedAt(token.createdAt());
        return entity;
    }

    private static RefreshToken toDomain(RefreshTokenJpaEntity entity) {
        return new RefreshToken(
                entity.getJti(),
                entity.getAccountId(),
                entity.getTokenHash(),
                entity.getExpiresAt(),
                entity.getRevokedAt(),
                entity.getCreatedAt());
    }
}
