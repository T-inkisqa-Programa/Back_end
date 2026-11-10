package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AccountJpaRepository
        extends JpaRepository<AccountJpaEntity, UUID>, JpaSpecificationExecutor<AccountJpaEntity> {

    Optional<AccountJpaEntity> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);
}
