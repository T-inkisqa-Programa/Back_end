package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.model.LoginAttempt;
import com.tinkisqa.iam.domain.port.LoginAttemptRepository;

/** Adaptador que implementa {@link LoginAttemptRepository} sobre JPA. */
@Component
public class LoginAttemptRepositoryAdapter implements LoginAttemptRepository {

    private final LoginAttemptJpaRepository repository;

    public LoginAttemptRepositoryAdapter(LoginAttemptJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<LoginAttempt> findByEmail(String email) {
        return repository.findById(email).map(entity -> new LoginAttempt(
                entity.getEmail(),
                entity.getAttempts(),
                entity.getLockedUntil(),
                entity.getUpdatedAt()));
    }

    @Override
    public LoginAttempt save(LoginAttempt attempt) {
        LoginAttemptJpaEntity entity = repository.findById(attempt.email()).orElseGet(LoginAttemptJpaEntity::new);
        entity.setEmail(attempt.email());
        entity.setAttempts(attempt.attempts());
        entity.setLockedUntil(attempt.lockedUntil());
        entity.setUpdatedAt(attempt.updatedAt());
        LoginAttemptJpaEntity saved = repository.save(entity);
        return new LoginAttempt(saved.getEmail(), saved.getAttempts(), saved.getLockedUntil(), saved.getUpdatedAt());
    }
}
