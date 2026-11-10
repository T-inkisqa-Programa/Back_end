-- =============================================================================
-- T'inkisqa — Aislamiento de datos por microservicio
--
-- Una sola instancia de PostgreSQL, pero una BASE DE DATOS independiente por
-- servicio. Ningún servicio consulta la base de otro: la comunicación es por
-- API REST (y eventos internos), nunca por SQL cruzado.
--
-- Este script lo ejecuta la imagen oficial de PostgreSQL la primera vez que se
-- inicializa el volumen (docker-entrypoint-initdb.d).
-- =============================================================================

CREATE DATABASE tinkisqa_iam;
CREATE DATABASE tinkisqa_social;
CREATE DATABASE tinkisqa_content;
CREATE DATABASE tinkisqa_service;
CREATE DATABASE tinkisqa_engagement;

-- Cada servicio se conecta con el mismo usuario propietario (POSTGRES_USER),
-- pero a su propia base de datos.
