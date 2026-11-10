package com.tinkisqa.iam.domain.port;

/** Puerto de salida: hashing y verificación de credenciales. */
public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String passwordHash);
}
