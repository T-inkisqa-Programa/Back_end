package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.social.domain.model.InteractionType;
import com.tinkisqa.social.domain.port.PostInteractionRepository;

/** Adaptador JPA del puerto {@link PostInteractionRepository}. */
@Component
public class PostInteractionRepositoryAdapter implements PostInteractionRepository {

    private final PostInteractionJpaRepository repository;

    public PostInteractionRepositoryAdapter(PostInteractionJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void add(UUID postId, UUID accountId, InteractionType type, Instant now) {
        if (repository.existsByPostIdAndAccountIdAndType(postId, accountId, type)) {
            return;
        }
        PostInteractionJpaEntity entity = new PostInteractionJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setPostId(postId);
        entity.setAccountId(accountId);
        entity.setType(type);
        entity.setCreatedAt(now);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void remove(UUID postId, UUID accountId, InteractionType type) {
        repository.deleteByPostIdAndAccountIdAndType(postId, accountId, type);
    }

    @Override
    public boolean exists(UUID postId, UUID accountId, InteractionType type) {
        return repository.existsByPostIdAndAccountIdAndType(postId, accountId, type);
    }

    @Override
    public long countByPost(UUID postId, InteractionType type) {
        return repository.countByPostIdAndType(postId, type);
    }

    @Override
    public Map<UUID, Long> countByPosts(Collection<UUID> postIds, InteractionType type) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : repository.countGroupedByPost(List.copyOf(postIds), type)) {
            counts.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    @Override
    public Set<UUID> postIdsMarkedBy(UUID accountId, InteractionType type, Collection<UUID> postIds) {
        if (postIds.isEmpty()) {
            return Set.of();
        }
        return new LinkedHashSet<>(repository.findPostIdsByAccountAndType(List.copyOf(postIds), accountId, type));
    }
}
