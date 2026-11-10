-- =============================================================================
-- IAM Service — esquema inicial (Fase 1)
--
-- Base de datos propia (tinkisqa_iam). Nunca se consulta desde otros servicios:
-- la comunicación es por API REST. La verificación documental guarda solo una
-- HUELLA no reversible del DNI (nunca el número en claro, ni fotografías).
-- =============================================================================

CREATE TABLE accounts (
    id                    UUID PRIMARY KEY,
    email                 VARCHAR(255) NOT NULL UNIQUE,
    password_hash         VARCHAR(255) NOT NULL,
    nombre_completo       VARCHAR(140) NOT NULL,
    role                  VARCHAR(32)  NOT NULL,
    account_status        VARCHAR(32)  NOT NULL,
    identity_status       VARCHAR(32)  NOT NULL,
    access_status         VARCHAR(32)  NOT NULL,
    email_verified        BOOLEAN      NOT NULL DEFAULT FALSE,
    normas_aceptadas      BOOLEAN      NOT NULL DEFAULT FALSE,
    bio                   VARCHAR(280),
    genero                VARCHAR(40),
    genero_personalizado  VARCHAR(60),
    avatar_uri            VARCHAR(512),
    created_at            TIMESTAMPTZ  NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL
);

CREATE TABLE account_interests (
    account_id UUID        NOT NULL REFERENCES accounts (id) ON DELETE CASCADE,
    interest   VARCHAR(60) NOT NULL,
    PRIMARY KEY (account_id, interest)
);

CREATE TABLE verifications (
    id              UUID PRIMARY KEY,
    account_id      UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    dni_fingerprint VARCHAR(128) NOT NULL,
    telefono        VARCHAR(20),
    state           VARCHAR(32)  NOT NULL,
    provider        VARCHAR(160),
    submitted_at    TIMESTAMPTZ  NOT NULL,
    resolved_at     TIMESTAMPTZ,
    notes           VARCHAR(500)
);

-- Anti-suplantación: una huella de DNI no puede usarse en dos cuentas.
CREATE UNIQUE INDEX ux_verifications_dni_fingerprint ON verifications (dni_fingerprint);
CREATE INDEX ix_verifications_account ON verifications (account_id);

CREATE TABLE otp_challenges (
    id          UUID PRIMARY KEY,
    target      VARCHAR(255) NOT NULL,
    channel     VARCHAR(16)  NOT NULL,
    purpose     VARCHAR(32)  NOT NULL,
    code_hash   VARCHAR(128) NOT NULL,
    attempts    INTEGER      NOT NULL DEFAULT 0,
    expires_at  TIMESTAMPTZ  NOT NULL,
    consumed_at TIMESTAMPTZ,
    created_at  TIMESTAMPTZ  NOT NULL
);
CREATE INDEX ix_otp_target ON otp_challenges (target, channel, purpose);

CREATE TABLE refresh_tokens (
    jti        UUID PRIMARY KEY,
    account_id UUID NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    token_hash VARCHAR(128) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL
);
CREATE INDEX ix_refresh_tokens_account ON refresh_tokens (account_id);

CREATE TABLE login_attempts (
    email        VARCHAR(255) PRIMARY KEY,
    attempts     INTEGER      NOT NULL DEFAULT 0,
    locked_until TIMESTAMPTZ,
    updated_at   TIMESTAMPTZ  NOT NULL
);

CREATE TABLE security_events (
    id         UUID PRIMARY KEY,
    account_id UUID,
    event_type VARCHAR(48)  NOT NULL,
    detail     VARCHAR(500),
    created_at TIMESTAMPTZ  NOT NULL
);
CREATE INDEX ix_security_events_account ON security_events (account_id, created_at DESC);
