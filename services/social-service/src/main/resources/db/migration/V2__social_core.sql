-- =============================================================================
-- Social Service — núcleo social (Fase 2)
--
-- Comunidades, membresías, solicitudes de unión, publicaciones, comentarios e
-- interacciones (me gusta / guardados).
--
-- Notas de diseño:
--   * El servicio NO guarda cuentas: solo identificadores (UUID) emitidos por el
--     IAM. El nombre y el avatar de la autora se guardan como "instantánea" en la
--     publicación/comunidad para poder pintarlas sin llamar al IAM.
--   * `communities.owner_id` NULL = comunidad de plataforma (la modera ADMIN).
--   * Me gusta y guardados comparten una sola tabla con `type` para no duplicar.
-- =============================================================================

-- ---------------------------------------------------------------- comunidades
CREATE TABLE communities (
    id          UUID         PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    description TEXT         NOT NULL DEFAULT '',
    image_url   TEXT,
    category    VARCHAR(60),
    privacy     VARCHAR(20)  NOT NULL DEFAULT 'PUBLICA',
    owner_id    UUID,
    created_at  TIMESTAMPTZ  NOT NULL,
    updated_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX ix_communities_owner ON communities (owner_id);
CREATE INDEX ix_communities_created ON communities (created_at DESC);

CREATE TABLE community_members (
    id           UUID        PRIMARY KEY,
    community_id UUID        NOT NULL REFERENCES communities (id) ON DELETE CASCADE,
    account_id   UUID        NOT NULL,
    joined_at    TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_community_members UNIQUE (community_id, account_id)
);

CREATE INDEX ix_community_members_account ON community_members (account_id);

CREATE TABLE community_join_requests (
    id           UUID        PRIMARY KEY,
    community_id UUID        NOT NULL REFERENCES communities (id) ON DELETE CASCADE,
    account_id   UUID        NOT NULL,
    requester_name VARCHAR(140),
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    created_at   TIMESTAMPTZ NOT NULL,
    resolved_at  TIMESTAMPTZ,
    resolved_by  UUID,
    CONSTRAINT uq_join_requests UNIQUE (community_id, account_id)
);

CREATE INDEX ix_join_requests_status ON community_join_requests (status);
CREATE INDEX ix_join_requests_community ON community_join_requests (community_id, status);
CREATE INDEX ix_join_requests_account ON community_join_requests (account_id, status);

-- -------------------------------------------------------------- publicaciones
CREATE TABLE posts (
    id            UUID        PRIMARY KEY,
    author_id     UUID        NOT NULL,
    author_name   VARCHAR(140),
    author_avatar TEXT,
    community_id  UUID        REFERENCES communities (id) ON DELETE SET NULL,
    tag           VARCHAR(60),
    content       TEXT        NOT NULL,
    is_quote      BOOLEAN     NOT NULL DEFAULT FALSE,
    image_url     TEXT,
    audio_url     TEXT,
    audio_name    VARCHAR(200),
    pdf_url       TEXT,
    pdf_name      VARCHAR(200),
    feeling       VARCHAR(60),
    activity      VARCHAR(60),
    location      VARCHAR(120),
    privacy       VARCHAR(20) NOT NULL DEFAULT 'PUBLICO',
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_posts_created ON posts (created_at DESC);
CREATE INDEX ix_posts_author ON posts (author_id, created_at DESC);
CREATE INDEX ix_posts_community ON posts (community_id, created_at DESC);

CREATE TABLE post_people (
    post_id UUID         NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    person  VARCHAR(140) NOT NULL
);

CREATE TABLE post_interactions (
    id         UUID        PRIMARY KEY,
    post_id    UUID        NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    account_id UUID        NOT NULL,
    type       VARCHAR(20) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_post_interactions UNIQUE (post_id, account_id, type)
);

CREATE INDEX ix_post_interactions_account ON post_interactions (account_id, type);

CREATE TABLE post_comments (
    id            UUID        PRIMARY KEY,
    post_id       UUID        NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
    author_id     UUID        NOT NULL,
    author_name   VARCHAR(140),
    author_avatar TEXT,
    text          TEXT        NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL
);

CREATE INDEX ix_post_comments_post ON post_comments (post_id, created_at);
CREATE INDEX ix_post_comments_author ON post_comments (author_id);
