package com.tinkisqa.social.domain.model;

/**
 * Publicación con los datos que dependen de quien consulta: número de me gusta y
 * comentarios, y si la cuenta ha dado me gusta o la ha guardado.
 *
 * <p>Se calcula en una sola pasada por página para no encadenar consultas.</p>
 */
public record PostView(
        Post post,
        long likeCount,
        long commentCount,
        long saveCount,
        boolean likedByViewer,
        boolean savedByViewer) {
}
