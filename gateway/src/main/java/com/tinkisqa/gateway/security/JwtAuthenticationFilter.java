package com.tinkisqa.gateway.security;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;

import com.tinkisqa.gateway.config.TinkisqaProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import reactor.core.publisher.Mono;

/**
 * Filtro transversal del Gateway:
 *
 * <ol>
 *   <li>Valida el JWT en todas las rutas protegidas (HS256, emisor y firma).</li>
 *   <li>Propaga la identidad a los microservicios en encabezados internos
 *       {@code X-User-Id}, {@code X-User-Email}, {@code X-User-Roles} y
 *       {@code X-User-Permissions}.</li>
 *   <li>Elimina SIEMPRE cualquier encabezado {@code X-User-*} enviado por el
 *       cliente para evitar suplantación.</li>
 *   <li>Añade un {@code X-Request-Id} para trazabilidad.</li>
 * </ol>
 */
@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";
    private static final String HEADER_REQUEST_ID = "X-Request-Id";

    private final TinkisqaProperties properties;
    private final SecretKey signingKey;

    public JwtAuthenticationFilter(TinkisqaProperties properties) {
        this.properties = properties;
        byte[] secret = properties.getSecurity().getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = new SecretKeySpec(secret, "HmacSHA256");
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = resolveRequestId(exchange);
        String path = exchange.getRequest().getPath().value();

        if (isPublic(path)) {
            return chain.filter(mutate(exchange, requestId, null, null, null, null));
        }

        String authorization = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return unauthorized(exchange, requestId, "Falta el encabezado Authorization con el token Bearer.");
        }

        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.getSecurity().getJwt().getIssuer())
                    .build()
                    .parseSignedClaims(authorization.substring(7))
                    .getPayload();

            String userId = claims.getSubject();
            if (userId == null || userId.isBlank()) {
                return unauthorized(exchange, requestId, "El token no contiene el sujeto (sub).");
            }

            return chain.filter(mutate(
                    exchange,
                    requestId,
                    userId,
                    asString(claims.get("email")),
                    asCsv(claims.get("roles")),
                    asCsv(claims.get("permissions"))));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Token rechazado en {}: {}", path, ex.getMessage());
            return unauthorized(exchange, requestId, "Token inválido o expirado.");
        }
    }

    private boolean isPublic(String path) {
        return properties.getGateway().getPublicPaths().stream()
                .anyMatch(pattern -> PATH_MATCHER.match(pattern, path));
    }

    private String resolveRequestId(ServerWebExchange exchange) {
        String requestId = exchange.getRequest().getHeaders().getFirst(HEADER_REQUEST_ID);
        return (requestId == null || requestId.isBlank()) ? UUID.randomUUID().toString() : requestId;
    }

    private ServerWebExchange mutate(
            ServerWebExchange exchange,
            String requestId,
            String userId,
            String email,
            String roles,
            String permissions) {

        ServerHttpRequest request = exchange.getRequest().mutate()
                .headers(headers -> {
                    // Nunca confiamos en encabezados de identidad enviados por el cliente.
                    headers.remove(HEADER_USER_ID);
                    headers.remove(HEADER_USER_EMAIL);
                    headers.remove(HEADER_USER_ROLES);
                    headers.remove(HEADER_USER_PERMISSIONS);

                    headers.set(HEADER_REQUEST_ID, requestId);
                    if (userId != null) {
                        headers.set(HEADER_USER_ID, userId);
                    }
                    if (email != null) {
                        headers.set(HEADER_USER_EMAIL, email);
                    }
                    if (roles != null) {
                        headers.set(HEADER_USER_ROLES, roles);
                    }
                    if (permissions != null) {
                        headers.set(HEADER_USER_PERMISSIONS, permissions);
                    }
                })
                .build();

        return exchange.mutate().request(request).build();
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String requestId, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        response.getHeaders().set(HEADER_REQUEST_ID, requestId);

        String body = "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}";
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    private static String asString(Object value) {
        return value == null ? null : value.toString();
    }

    private static String asCsv(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse(null);
        }
        return value == null ? null : value.toString();
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
