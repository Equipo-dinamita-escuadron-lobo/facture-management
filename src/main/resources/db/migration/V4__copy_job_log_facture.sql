-- DDL para el log de idempotencia de copia del módulo facture.
-- MySQL: usa DATETIME(6) en lugar de TIMESTAMPTZ (PostgreSQL).
-- ADR-38.

CREATE TABLE IF NOT EXISTS facture_copy_job_log (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
  id_proceso             CHAR(36)       NOT NULL,
  fase                   INT            NOT NULL,
  modulo                 VARCHAR(64)    NOT NULL,
  estado                 VARCHAR(32)    NOT NULL,
  fecha_inicio           DATETIME(6),
  fecha_fin              DATETIME(6),
  equivalencias_generadas INT           NOT NULL DEFAULT 0,
  error_message          VARCHAR(2000),
  CONSTRAINT uq_facture_copy_job_log UNIQUE (id_proceso, fase, modulo)
);

CREATE INDEX IF NOT EXISTS idx_facture_copy_job_log_id_proceso
    ON facture_copy_job_log (id_proceso);
