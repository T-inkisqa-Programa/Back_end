package com.tinkisqa.social.domain.port;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.tinkisqa.social.domain.model.InteractionType;

/** Puerto de salida: interacciones (me gusta / guardados) con publicaciones. */
public interface PostInteractionRepository {

    void add(UUID postId, UUID accountId, InteractionType type, Instant now);

    void remove(UUID postId, UUID accountId, InteractionType type);

    boolean exists(UUID postId, UUID accountId, InteractionType type);

    long countByPost(UUID postId, InteractionType type);

    /** Contadores por publicación, en una sola consulta (evita N+1). */
    Map<UUID, Long> countByPosts(Collection<UUID> postIds, InteractionType type);

    /** Publicaciones de la página que esta cuenta ya marcó con ese tipo. */
    Set<UUID> postIdsMarkedBy(UUID accountId, InteractionType type, Collection<UUID> postIds);
}
