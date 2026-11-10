package com.tinkisqa.iam.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Denuncia realizada por una usuaria contra otra.
 *
 * <p>Es un registro inmutable: la resolución crea una nueva instancia con
 * {@link #resolved}. Nunca se borra, para conservar la trazabilidad de
 * moderación.</p>
 */
public record Report(
        UUID id,
        UUID reporterId,
        ReportTargetType targetType,
        UUID targetId,
        String reason,
        ReportStatus status,
        UUID reviewerId,
        String resolution,
        Instant createdAt,
        Instant resolvedAt) {

    public static Report opened(
            UUID reporterId,
            ReportTargetType targetType,
            UUID targetId,
            String reason,
            Instant now) {
        return new Report(UUID.randomUUID(), reporterId, targetType, targetId, reason,
                ReportStatus.ABIERTO, null, null, now, null);
    }

    public Report resolved(ReportStatus newStatus, UUID reviewerId, String resolution, Instant now) {
        return new Report(id, reporterId, targetType, targetId, reason,
                newStatus, reviewerId, resolution, createdAt, now);
    }

    public boolean isOpen() {
        return status == ReportStatus.ABIERTO;
    }
}
