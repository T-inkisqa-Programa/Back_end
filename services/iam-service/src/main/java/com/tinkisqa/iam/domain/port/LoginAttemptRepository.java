package com.tinkisqa.iam.domain.port;

import java.util.Optional;

import com.tinkisqa.iam.domain.model.LoginAttempt;

/** Puerto de salida: control de intentos de inicio de sesión por correo. */
public interface LoginAttemptRepository {

    Optional<LoginAttempt> findByEmail(String email);

    LoginAttempt save(LoginAttempt attempt);
}
