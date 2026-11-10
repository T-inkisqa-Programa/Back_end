package com.tinkisqa.social.domain.model;

/**
 * Interacción de una cuenta con una publicación. Me gusta y guardado comparten
 * tabla y adaptador para no duplicar lógica.
 */
public enum InteractionType {
    LIKE,
    SAVE
}
