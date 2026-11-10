package com.tinkisqa.social.domain.model;

/**
 * Comunidad con los datos que dependen de quien consulta: número de integrantes,
 * si ya pertenece, si tiene una solicitud pendiente y si es su dueña.
 */
public record CommunityView(
        Community community,
        long memberCount,
        boolean member,
        boolean pendingRequest,
        boolean owner) {
}
