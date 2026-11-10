package com.tinkisqa.iam.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.tinkisqa.iam.domain.exception.DomainException;
import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.Permission;
import com.tinkisqa.iam.domain.port.TokenIssuer;
import com.tinkisqa.iam.application.config.IamProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * Emisión y lectura de JWT con HS256.
 *
 * <p>Los claims que consume el API Gateway son: {@code sub} (id de la cuenta),
 * {@code roles}, {@code permissions} y {@code email}.</p>
 */
@Component
public class JwtTokenIssuer implements TokenIssuer {

    private final IamProperties properties;
    private final SecretKey signingKey;

    public JwtTokenIssuer(IamProperties properties) {
        this.properties = properties;
        byte[] secret = properties.getSecurity().getJwt().getSecret().getBytes(StandardCharsets.UTF_8);
        this.signingKey = Keys.hmacShaKeyFor(secret);
    }

    @Override
    public IssuedTokens issueTokens(Account account, Set<Permission> permissions) {
        Instant now = Instant.now();
        Duration accessTtl = properties.getIam().getAccessTokenTtl();
        Duration refreshTtl = properties.getIam().getRefreshTokenTtl();
        String issuer = properties.getSecurity().getJwt().getIssuer();
        String jti = UUID.randomUUID().toString();

        var accessBuilder = Jwts.builder()
                .subject(account.id().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(accessTtl)))
                .claim("email", account.email())
                .claim("nombreCompleto", account.nombreCompleto())
                .claim("roles", List.of(account.role().name()))
                .claim("permissions", serializePermissions(permissions))
                .claim("type", "access");

        // El avatar solo se incluye si existe: el resto de servicios lo usan para
        // pintar el contenido sin consultar al IAM.
        if (account.avatarUri() != null && !account.avatarUri().isBlank()) {
            accessBuilder.claim("avatarUri", account.avatarUri());
        }

        String accessToken = accessBuilder.signWith(signingKey, Jwts.SIG.HS256).compact();

        String refreshToken = Jwts.builder()
                .subject(account.id().toString())
                .id(jti)
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(refreshTtl)))
                .claim("type", "refresh")
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();

        return new IssuedTokens(accessToken, refreshToken, jti, accessTtl.getSeconds());
    }

    @Override
    public ParsedToken parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .requireIssuer(properties.getSecurity().getJwt().getIssuer())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return new ParsedToken(
                    UUID.fromString(claims.getSubject()),
                    String.valueOf(claims.get("type")),
                    claims.getId());
        } catch (JwtException | IllegalArgumentException ex) {
            throw DomainException.unauthorized("Token inválido o expirado.");
        }
    }

    @Override
    public String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible.", ex);
        }
    }

    @Override
    public List<String> serializePermissions(Set<Permission> permissions) {
        return permissions.stream().map(Enum::name).sorted().toList();
    }
}
