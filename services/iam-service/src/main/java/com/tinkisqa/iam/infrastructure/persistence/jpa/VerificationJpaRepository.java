package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.tinkisqa.iam.domain.model.VerificationState;

public interface VerificationJpaRepository extends JpaRepository<VerificationJpaEntity, UUID> {

    Optional<VerificationJpaEntity> findFirstByAccountIdOrderBySubmittedAtDesc(UUID accountId);

    boolean existsByDniFingerprint(String dniFingerprint);

    List<VerificationJpaEntity> findByStateOrderBySubmittedAtAsc(VerificationState state, Pageable pageable);

    List<VerificationJpaEntity> findByStateInOrderBySubmittedAtAsc(Collection<VerificationState> states,
            Pageable pageable);
}
