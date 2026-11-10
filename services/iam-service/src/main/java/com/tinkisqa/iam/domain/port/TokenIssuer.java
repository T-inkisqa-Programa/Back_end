package com.tinkisqa.iam.domain.port;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.tinkisqa.iam.domain.model.Account;
import com.tinkisqa.iam.domain.model.Permission;

/**
 * Puerto de salida: emisión y lectura de tokens JWT.
 *
 * <p>Los tokens se firman con HS256 usando un secreto compartido con el API
 * Gateway y el resto de microservicios (contrato único de identidad).</p>
 */
public interface TokenIssuer {

    record IssuedTokens(String accessToken, String refreshToken, String refreshJti, long expiresInSeconds) {
    }

    record ParsedToken(UUID accountId, String type, String jti) {
    }

    IssuedTokens issueTokens(Account account, Set<Permission> permissions);

    ParsedToken parse(String token);

    String hashToken(String token);

    List<String> serializePermissions(Set<Permission> permissions);
}
