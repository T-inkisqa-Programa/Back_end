-- =============================================================================
-- IAM Service — dos roles globales y denuncias persistentes
--
-- 1) El sistema pasa a tener EXACTAMENTE dos roles globales: ROLE_USER y
--    ROLE_ADMIN. Cualquier cuenta con el antiguo ROLE_MODERATOR pasa a
--    ROLE_ADMIN para no perder capacidades de moderación.
-- 2) Las denuncias dejan de ser solo un evento de auditoría: se guardan en su
--    propia tabla para que la administración pueda listarlas y resolverlas.
-- =============================================================================

UPDATE accounts SET role = 'ROLE_ADMIN' WHERE role = 'ROLE_MODERATOR';

CREATE TABLE reports (
    id          UUID PRIMARY KEY,
    reporter_id UUID         NOT NULL,
    target_type VARCHAR(32)  NOT NULL,
    target_id   UUID         NOT NULL,
    reason      VARCHAR(500) NOT NULL,
    status      VARCHAR(32)  NOT NULL,
    reviewer_id UUID,
    resolution  VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL,
    resolved_at TIMESTAMPTZ
);

CREATE INDEX ix_reports_status ON reports (status, created_at DESC);
CREATE INDEX ix_reports_target ON reports (target_type, target_id);
CREATE INDEX ix_reports_reporter ON reports (reporter_id, created_at DESC);
