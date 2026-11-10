package com.tinkisqa.iam.infrastructure.persistence.jpa;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.model.SecurityEvent;
import com.tinkisqa.iam.domain.port.SecurityEventRepository;

/** Adaptador que implementa {@link SecurityEventRepository} sobre JPA. */
@Component
public class SecurityEventRepositoryAdapter implements SecurityEventRepository {

    private final SecurityEventJpaRepository repository;

    public SecurityEventRepositoryAdapter(SecurityEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public SecurityEvent save(SecurityEvent event) {
        SecurityEventJpaEntity entity = new SecurityEventJpaEntity();
        entity.setId(event.id());
        entity.setAccountId(event.accountId());
        entity.setEventType(event.type());
        entity.setDetail(event.detail());
        entity.setCreatedAt(event.createdAt());
        return toDomain(repository.save(entity));
    }

    @Override
    public List<SecurityEvent> findRecentByAccount(UUID accountId, int limit) {
        return repository.findByAccountIdOrderByCreatedAtDesc(accountId, PageRequest.of(0, limit))
                .stream()
                .map(SecurityEventRepositoryAdapter::toDomain)
                .toList();
    }

    private static SecurityEvent toDomain(SecurityEventJpaEntity entity) {
        return new SecurityEvent(
                entity.getId(),
                entity.getAccountId(),
                entity.getEventType(),
                entity.getDetail(),
                entity.getCreatedAt());
    }
}
