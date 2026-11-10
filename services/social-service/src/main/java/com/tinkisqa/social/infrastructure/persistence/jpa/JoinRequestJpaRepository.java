package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tinkisqa.social.domain.model.JoinRequestStatus;

public interface JoinRequestJpaRepository extends JpaRepository<JoinRequestJpaEntity, UUID> {

    Optional<JoinRequestJpaEntity> findByCommunityIdAndAccountId(UUID communityId, UUID accountId);

    List<JoinRequestJpaEntity> findByCommunityIdAndStatusOrderByCreatedAtAsc(
            UUID communityId, JoinRequestStatus status);

    List<JoinRequestJpaEntity> findByAccountIdAndStatus(UUID accountId, JoinRequestStatus status);
}
