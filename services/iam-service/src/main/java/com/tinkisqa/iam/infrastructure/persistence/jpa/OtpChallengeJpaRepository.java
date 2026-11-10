package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tinkisqa.iam.domain.model.OtpChallenge;

public interface OtpChallengeJpaRepository extends JpaRepository<OtpChallengeJpaEntity, UUID> {

    Optional<OtpChallengeJpaEntity> findFirstByTargetAndChannelAndPurposeAndConsumedAtIsNullOrderByCreatedAtDesc(
            String target, OtpChallenge.Channel channel, OtpChallenge.Purpose purpose);

    List<OtpChallengeJpaEntity> findByTargetAndChannelAndPurposeAndConsumedAtIsNull(
            String target, OtpChallenge.Channel channel, OtpChallenge.Purpose purpose);
}
