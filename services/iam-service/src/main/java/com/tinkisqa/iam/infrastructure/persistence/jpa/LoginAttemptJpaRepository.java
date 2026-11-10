package com.tinkisqa.iam.infrastructure.persistence.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptJpaRepository extends JpaRepository<LoginAttemptJpaEntity, String> {
}
