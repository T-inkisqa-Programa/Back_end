package com.tinkisqa.social.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Comunidad (foro). Registro inmutable del dominio: los cambios se expresan
 * creando una instancia nueva con {@code with...}.
 *
 * <p>Una comunidad con {@code ownerId} nulo es de la plataforma: solo la
 * administración global (ROLE_ADMIN) puede moderarla. Las comunidades creadas
 * por una usuaria pertenecen a ella.</p>
 */
public record Community(
        UUID id,
        String name,
        String description,
        String imageUrl,
        String category,
        CommunityPrivacy privacy,
        UUID ownerId,
        Instant createdAt,
        Instant updatedAt) {

    public static Community created(
            UUID id,
            String name,
            String description,
            String imageUrl,
            String category,
            CommunityPrivacy privacy,
            UUID ownerId,
            Instant now) {
        return new Community(id, name, description, imageUrl, category, privacy, ownerId, now, now);
    }

    public Community withDetails(
            String name,
            String description,
            String imageUrl,
            String category,
            CommunityPrivacy privacy,
            Instant now) {
        return new Community(id, name, description, imageUrl, category, privacy, ownerId, createdAt, now);
    }

    /** ¿La comunidad pertenece a esta cuenta? */
    public boolean isOwnedBy(UUID accountId) {
        return ownerId != null && ownerId.equals(accountId);
    }

    /** Comunidad de plataforma: sin dueña, la modera la administración global. */
    public boolean isPlatformOwned() {
        return ownerId == null;
    }
}
