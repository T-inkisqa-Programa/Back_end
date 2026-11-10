-- =============================================================================
-- Service Service — migración base (Fase 0)
--
-- Base de datos propia (tinkisqa_service). El esquema funcional (red de apoyo,
-- psicólogas, oportunidades de voluntariado y postulaciones) se añadirá en la
-- Fase 4.
-- =============================================================================

CREATE TABLE schema_marker (
    id         SMALLINT PRIMARY KEY,
    service    VARCHAR(64) NOT NULL,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO schema_marker (id, service) VALUES (1, 'service-service');
