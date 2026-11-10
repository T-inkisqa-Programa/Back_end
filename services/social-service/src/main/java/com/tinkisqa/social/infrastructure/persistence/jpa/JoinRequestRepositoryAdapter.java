package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.tinkisqa.social.domain.model.JoinRequest;
import com.tinkisqa.social.domain.model.JoinRequestStatus;
import com.tinkisqa.social.domain.port.JoinRequestRepository;

/** Adaptador JPA del puerto {@link JoinRequestRepository}. */
@Component
public class JoinRequestRepositoryAdapter implements JoinRequestRepository {

    private final JoinRequestJpaRepository repository;

    public JoinRequestRepositoryAdapter(JoinRequestJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public JoinRequest save(JoinRequest request) {
        return toDomain(repository.save(toEntity(request)));
    }

    @Override
    public Optional<JoinRequest> findById(UUID id) {
        return repository.findById(id).map(JoinRequestRepositoryAdapter::toDomain);
    }

    @Override
    public Optional<JoinRequest> findByCommunityAndAccount(UUID communityId, UUID accountId) {
        return repository.findByCommunityIdAndAccountId(communityId, accountId)
                .map(JoinRequestRepositoryAdapter::toDomain);
    }

    @Override
    public List<JoinRequest> findPendingByCommunity(UUID communityId) {
        return repository.findByCommunityIdAndStatusOrderByCreatedAtAsc(communityId, JoinRequestStatus.PENDIENTE)
                .stream()
                .map(JoinRequestRepositoryAdapter::toDomain)
                .toList();
    }

    @Override
    public Set<UUID> pendingCommunityIdsOf(UUID accountId) {
        return repository.findByAccountIdAndStatus(accountId, JoinRequestStatus.PENDIENTE).stream()
                .map(JoinRequestJpaEntity::getCommunityId)
                .collect(Collectors.toSet());
    }

    private static JoinRequestJpaEntity toEntity(JoinRequest request) {
        JoinRequestJpaEntity entity = new JoinRequestJpaEntity();
        entity.setId(request.id());
        entity.setCommunityId(request.communityId());
        entity.setAccountId(request.accountId());
        entity.setRequesterName(request.requesterName());
        entity.setStatus(request.status());
        entity.setCreatedAt(request.createdAt());
        entity.setResolvedAt(request.resolvedAt());
        entity.setResolvedBy(request.resolvedBy());
        return entity;
    }

    private static JoinRequest toDomain(JoinRequestJpaEntity entity) {
        return new JoinRequest(
                entity.getId(),
                entity.getCommunityId(),
                entity.getAccountId(),
                entity.getRequesterName(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getResolvedAt(),
                entity.getResolvedBy());
    }
}
