package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.model.OtpChallenge;
import com.tinkisqa.iam.domain.port.OtpChallengeRepository;

/** Adaptador que implementa {@link OtpChallengeRepository} sobre JPA. */
@Component
public class OtpChallengeRepositoryAdapter implements OtpChallengeRepository {

    private final OtpChallengeJpaRepository repository;

    public OtpChallengeRepositoryAdapter(OtpChallengeJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public OtpChallenge save(OtpChallenge challenge) {
        return toDomain(repository.save(toEntity(challenge)));
    }

    @Override
    public Optional<OtpChallenge> findLatestActive(
            String target, OtpChallenge.Channel channel, OtpChallenge.Purpose purpose) {
        return repository
                .findFirstByTargetAndChannelAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(target, channel, purpose)
                .map(OtpChallengeRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<UUID> findLatestId(
            String target, OtpChallenge.Channel channel, OtpChallenge.Purpose purpose) {
        return repository
                .findFirstByTargetAndChannelAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(target, channel, purpose)
                .map(OtpChallengeJpaEntity::getId);
    }

    @Override
    public void invalidateAll(String target, OtpChallenge.Channel channel, OtpChallenge.Purpose purpose) {
        List<OtpChallengeJpaEntity> active =
                repository.findByTargetAndChannelAndPurposeAndConsumedAtIsNull(target, channel, purpose);
        if (active.isEmpty()) {
            return;
        }
        Instant now = Instant.now();
        active.forEach(entity -> entity.setConsumedAt(now));
        repository.saveAll(active);
    }

    private static OtpChallengeJpaEntity toEntity(OtpChallenge challenge) {
        OtpChallengeJpaEntity entity = new OtpChallengeJpaEntity();
        entity.setId(challenge.id());
        entity.setTarget(challenge.target());
        entity.setChannel(challenge.channel());
        entity.setPurpose(challenge.purpose());
        entity.setCodeHash(challenge.codeHash());
        entity.setAttempts(challenge.attempts());
        entity.setExpiresAt(challenge.expiresAt());
        entity.setConsumedAt(challenge.consumedAt());
        entity.setCreatedAt(challenge.createdAt());
        return entity;
    }

    private static OtpChallenge toDomain(OtpChallengeJpaEntity entity) {
        return new OtpChallenge(
                entity.getId(),
                entity.getTarget(),
                entity.getChannel(),
                entity.getPurpose(),
                entity.getCodeHash(),
                entity.getAttempts(),
                entity.getExpiresAt(),
                entity.getConsumedAt(),
                entity.getCreatedAt());
    }
}
