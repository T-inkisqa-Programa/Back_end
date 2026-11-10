package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenJpaEntity, UUID> {

    List<RefreshTokenJpaEntity> findByAccountIdAndRevokedAtIsNull(UUID accountId);
}
