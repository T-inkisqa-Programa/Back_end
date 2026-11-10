package com.tinkisqa.social.presentation.security;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Acceso al usuario autenticado a partir del JWT validado.
 *
 * <p>El identificador de la usuaria llega en el claim {@code sub}; el nombre
 * visible en {@code nombreCompleto}, el avatar en {@code avatarUri} y el rol
 * global en {@code roles}.</p>
 */
@Component
public class CurrentUser {

    public Optional<UUID> accountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return Optional.of(UUID.fromString(token.getToken().getSubject()));
        }
        return Optional.empty();
    }

    public UUID requireAccountId() {
        return accountId().orElseThrow(() -> new IllegalStateException("No hay una usuaria autenticada."));
    }

    public Jwt jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return token.getToken();
        }
        throw new IllegalStateException("No hay una usuaria autenticada.");
    }

    /** Nombre visible de la autora, tal como viaja en el token. */
    public String displayName() {
        return jwt().getClaimAsString("nombreCompleto");
    }

    /** Avatar de la autora, tal como viaja en el token (puede ser nulo). */
    public String avatar() {
        return jwt().getClaimAsString("avatarUri");
    }

    /** ¿La sesión tiene el rol global de administración? */
    public boolean isAdmin() {
        List<String> roles = jwt().getClaimAsStringList("roles");
        return roles != null && roles.contains("ROLE_ADMIN");
    }
}
