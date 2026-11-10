package com.tinkisqa.social.infrastructure.persistence.jpa;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.tinkisqa.social.domain.port.CommunityMembershipRepository;

/** Adaptador JPA del puerto {@link CommunityMembershipRepository}. */
@Component
public class CommunityMembershipAdapter implements CommunityMembershipRepository {

    private final CommunityMemberJpaRepository repository;

    public CommunityMembershipAdapter(CommunityMemberJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void add(UUID communityId, UUID accountId, Instant now) {
        if (repository.existsByCommunityIdAndAccountId(communityId, accountId)) {
            return;
        }
        CommunityMemberJpaEntity entity = new CommunityMemberJpaEntity();
        entity.setId(UUID.randomUUID());
        entity.setCommunityId(communityId);
        entity.setAccountId(accountId);
        entity.setJoinedAt(now);
        repository.save(entity);
    }

    @Override
    @Transactional
    public void remove(UUID communityId, UUID accountId) {
        repository.deleteByCommunityIdAndAccountId(communityId, accountId);
    }

    @Override
    public boolean exists(UUID communityId, UUID accountId) {
        return repository.existsByCommunityIdAndAccountId(communityId, accountId);
    }

    @Override
    public long countByCommunity(UUID communityId) {
        return repository.countByCommunityId(communityId);
    }

    @Override
    public Map<UUID, Long> countByCommunities(Collection<UUID> communityIds) {
        if (communityIds.isEmpty()) {
            return Map.of();
        }
        Map<UUID, Long> counts = new HashMap<>();
        for (Object[] row : repository.countGroupedByCommunity(List.copyOf(communityIds))) {
            counts.put((UUID) row[0], ((Number) row[1]).longValue());
        }
        return counts;
    }

    @Override
    public Set<UUID> communityIdsOf(UUID accountId) {
        return repository.findByAccountId(accountId).stream()
                .map(CommunityMemberJpaEntity::getCommunityId)
                .collect(Collectors.toSet());
    }
}
