package com.tinkisqa.iam.domain.model;

/**
 * Rol global de la cuenta (autoridad de Spring Security, con prefijo ROLE_).
 *
 * <p>El sistema tiene exactamente <strong>dos roles globales</strong>:</p>
 * <ul>
 *   <li>{@link #ROLE_USER}: usuaria de la comunidad.</li>
 *   <li>{@link #ROLE_ADMIN}: equipo de administración y moderación.</li>
 * </ul>
 *
 * <p>La administración de una comunidad concreta no es un rol global: se
 * resuelve con la pertenencia a esa comunidad (ver el Social Service).</p>
 */
public enum Role {
    ROLE_USER,
    ROLE_ADMIN
}
