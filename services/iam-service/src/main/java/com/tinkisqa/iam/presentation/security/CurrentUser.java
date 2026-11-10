package com.tinkisqa.iam.presentation.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.exception.DomainException;

/**
 * Acceso a la identidad de la petición actual.
 *
 * <p>El Gateway valida el JWT y lo reenvía; el IAM lo vuelve a validar con su
 * propia configuración de resource server. El identificador de la cuenta viaja
 * en el claim {@code sub}.</p>
 */
@Component
public class CurrentUser {

    public Optional<UUID> accountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return Optional.of(UUID.fromString(jwtAuthentication.getToken().getSubject()));
        }
        return Optional.empty();
    }

    public UUID requireAccountId() {
        return accountId().orElseThrow(() -> DomainException.unauthorized("La petición no está autenticada."));
    }

    public Jwt jwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return jwtAuthentication.getToken();
        }
        throw DomainException.unauthorized("La petición no está autenticada.");
    }
}
