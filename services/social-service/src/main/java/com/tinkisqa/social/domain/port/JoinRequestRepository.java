package com.tinkisqa.social.domain.port;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.tinkisqa.social.domain.model.JoinRequest;

/** Puerto de salida: solicitudes de ingreso a comunidades. */
public interface JoinRequestRepository {

    JoinRequest save(JoinRequest request);

    Optional<JoinRequest> findById(UUID id);

    Optional<JoinRequest> findByCommunityAndAccount(UUID communityId, UUID accountId);

    List<JoinRequest> findPendingByCommunity(UUID communityId);

    /** Comunidades donde esta cuenta tiene una solicitud pendiente. */
    Set<UUID> pendingCommunityIdsOf(UUID accountId);
}
