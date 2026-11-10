package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecurityEventJpaRepository extends JpaRepository<SecurityEventJpaEntity, UUID> {

    java.util.List<SecurityEventJpaEntity> findByAccountIdOrderByCreatedAtDesc(UUID accountId,
            org.springframework.data.domain.Pageable pageable);
}
