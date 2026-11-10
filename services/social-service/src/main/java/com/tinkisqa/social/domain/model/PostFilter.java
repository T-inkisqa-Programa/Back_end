package com.tinkisqa.social.domain.model;

import java.util.UUID;

/**
 * Filtro del muro. Todos los campos son opcionales: se combinan con AND.
 *
 * @param query       texto libre sobre el contenido (búsqueda)
 * @param communityId comunidad concreta
 * @param authorId    publicaciones de una autora
 * @param savedBy     solo las guardadas por esta cuenta (muro de guardados)
 */
public record PostFilter(String query, UUID communityId, UUID authorId, UUID savedBy) {

    public static PostFilter none() {
        return new PostFilter(null, null, null, null);
    }
}
