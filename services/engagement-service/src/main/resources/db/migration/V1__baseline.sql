-- =============================================================================
-- Engagement Service — migración base (Fase 0)
--
-- Base de datos propia (tinkisqa_engagement). El esquema funcional (rachas y
-- su historial de días, notificaciones, métricas y actividad) se añadirá en la
-- Fase 5.
-- =============================================================================

CREATE TABLE schema_marker (
    id         SMALLINT PRIMARY KEY,
    service    VARCHAR(64) NOT NULL,
    applied_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO schema_marker (id, service) VALUES (1, 'engagement-service');
