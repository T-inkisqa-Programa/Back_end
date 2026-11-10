package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CommunityJpaRepository
        extends JpaRepository<CommunityJpaEntity, UUID>, JpaSpecificationExecutor<CommunityJpaEntity> {
}
