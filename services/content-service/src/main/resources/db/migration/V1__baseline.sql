-- =============================================================================
-- Content Service — migración base (Fase 0)
--
-- Base de datos propia (tinkisqa_content). El esquema funcional (artículos,
-- bloques, recursos educativos y biblioteca de bienestar) se añadirá en la Fase 3.
-- =============================================================================

CREATE TABLE schema_marker (
    id         SMALLINT PRIMARY KEY,
    service    VARCHAR(64) NOT NULL,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO schema_marker (id, service) VALUES (1, 'content-service');
