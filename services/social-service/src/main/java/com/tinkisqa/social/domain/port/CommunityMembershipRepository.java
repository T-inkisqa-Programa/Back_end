package com.tinkisqa.social.domain.port;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Puerto de salida: membresías de comunidades. */
public interface CommunityMembershipRepository {

    void add(UUID communityId, UUID accountId, Instant now);

    void remove(UUID communityId, UUID accountId);

    boolean exists(UUID communityId, UUID accountId);

    long countByCommunity(UUID communityId);

    /** Integrantes por comunidad, en una sola consulta (evita N+1 en los listados). */
    Map<UUID, Long> countByCommunities(Collection<UUID> communityIds);

    Set<UUID> communityIdsOf(UUID accountId);
}
