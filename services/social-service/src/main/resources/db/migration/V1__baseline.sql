-- =============================================================================
-- Social Service — migración base (Fase 0)
--
-- Cada microservicio tiene su PROPIA base de datos (tinkisqa_social) y gestiona
-- su esquema con Flyway. El esquema funcional (comunidades, foros, publicaciones,
-- comentarios, likes, guardados y testimonios) se añadirá en la Fase 2.
-- =============================================================================

CREATE TABLE schema_marker (
    id         SMALLINT PRIMARY KEY,
    service    VARCHAR(64) NOT NULL,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO schema_marker (id, service) VALUES (1, 'social-service');
