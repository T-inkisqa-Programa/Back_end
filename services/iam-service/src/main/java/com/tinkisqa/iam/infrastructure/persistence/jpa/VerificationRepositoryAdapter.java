package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.model.Verification;
import com.tinkisqa.iam.domain.model.VerificationState;
import com.tinkisqa.iam.domain.port.VerificationRepository;

/** Adaptador que implementa {@link VerificationRepository} sobre JPA. */
@Component
public class VerificationRepositoryAdapter implements VerificationRepository {

    private final VerificationJpaRepository repository;

    public VerificationRepositoryAdapter(VerificationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Verification save(Verification verification) {
        return toDomain(repository.save(toEntity(verification)));
    }

    @Override
    public Optional<Verification> findById(UUID id) {
        return repository.findById(id).map(VerificationRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<Verification> findLatestByAccountId(UUID accountId) {
        return repository.findFirstByAccountIdOrderBySubmittedAtDesc(accountId)
                .map(VerificationRepositoryAdapter::toDomain);
    }

    @Override
    public boolean existsByDniFingerprint(String dniFingerprint) {
        return repository.existsByDniFingerprint(dniFingerprint);
    }

    @Override
    public List<Verification> findByState(VerificationState state, int limit) {
        return repository.findByStateOrderBySubmittedAtAsc(state, PageRequest.of(0, Math.max(1, limit))).stream()
                .map(VerificationRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public List<Verification> findOpen(int limit) {
        List<VerificationState> open = List.of(VerificationState.PENDIENTE, VerificationState.REVISION_HUMANA);
        return repository.findByStateInOrderBySubmittedAtAsc(open, PageRequest.of(0, Math.max(1, limit))).stream()
                .map(VerificationRepositoryAdapter::toDomain)
                .toList();
    }

    private static VerificationJpaEntity toEntity(Verification v) {
        VerificationJpaEntity entity = new VerificationJpaEntity();
        entity.setId(v.id());
        entity.setAccountId(v.accountId());
        entity.setDniFingerprint(v.dniFingerprint());
        entity.setTelefono(v.telefono());
        entity.setState(v.state());
        entity.setProvider(v.provider());
        entity.setSubmittedAt(v.submittedAt());
        entity.setResolvedAt(v.resolvedAt());
        entity.setNotes(v.notes());
        return entity;
    }

    private static Verification toDomain(VerificationJpaEntity entity) {
        return new Verification(
                entity.getId(),
                entity.getAccountId(),
                entity.getDniFingerprint(),
                entity.getTelefono(),
                entity.getState(),
                entity.getProvider(),
                entity.getSubmittedAt(),
                entity.getResolvedAt(),
                entity.getNotes());
    }
}
