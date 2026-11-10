package com.tinkisqa.service.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Acceso al usuario autenticado a partir del JWT validado.
 * El identificador de la usuaria llega en el claim {@code sub}.
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
}
