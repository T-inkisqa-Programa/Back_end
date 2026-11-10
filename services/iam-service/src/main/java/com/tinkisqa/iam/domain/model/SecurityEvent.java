package com.tinkisqa.iam.domain.model;

import java.time.Instant;
import java.util.UUID;

/** Evento de seguridad (auditoría) asociado a una cuenta o a un intento. */
public record SecurityEvent(
        UUID id,
        UUID accountId,
        SecurityEventType type,
        String detail,
        Instant createdAt) {

    public static SecurityEvent of(UUID accountId, SecurityEventType type, String detail, Instant now) {
        return new SecurityEvent(UUID.randomUUID(), accountId, type, detail, now);
    }
}
