package com.tinkisqa.social.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Solicitud de ingreso a una comunidad. Nadie entra sin una decisión explícita
 * de quien modera: la solicitud nace en {@link JoinRequestStatus#PENDIENTE} y
 * solo quien modera puede aprobarla o rechazarla.
 */
public record JoinRequest(
        UUID id,
        UUID communityId,
        UUID accountId,
        String requesterName,
        JoinRequestStatus status,
        Instant createdAt,
        Instant resolvedAt,
        UUID resolvedBy) {

    public static JoinRequest opened(
            UUID id, UUID communityId, UUID accountId, String requesterName, Instant now) {
        return new JoinRequest(
                id, communityId, accountId, requesterName, JoinRequestStatus.PENDIENTE, now, null, null);
    }

    public boolean isPending() {
        return status == JoinRequestStatus.PENDIENTE;
    }

    public JoinRequest approved(UUID resolvedBy, Instant now) {
        return new JoinRequest(
                id, communityId, accountId, requesterName, JoinRequestStatus.APROBADA, createdAt, now, resolvedBy);
    }

    public JoinRequest rejected(UUID resolvedBy, Instant now) {
        return new JoinRequest(
                id, communityId, accountId, requesterName, JoinRequestStatus.RECHAZADA, createdAt, now, resolvedBy);
    }

    /** Vuelve a quedar pendiente (la usuaria puede reintentar tras un rechazo). */
    public JoinRequest reopened(Instant now) {
        return new JoinRequest(
                id, communityId, accountId, requesterName, JoinRequestStatus.PENDIENTE, now, null, null);
    }
}
