ALTER TABLE skeleton_factures DROP CONSTRAINT IF EXISTS skeleton_factures_fact_code_key;
ALTER TABLE skeleton_factures ADD COLUMN IF NOT EXISTS purchase_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE skeleton_factures ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE skeleton_factures ADD COLUMN IF NOT EXISTS tenant_id VARCHAR(80);
ALTER TABLE skeleton_factures ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE skeleton_factures ADD COLUMN IF NOT EXISTS voided_at TIMESTAMP;
UPDATE skeleton_factures SET updated_at = COALESCE(updated_at, created_at), tenant_id = COALESCE(tenant_id, 'legacy-unassigned');
ALTER TABLE skeleton_factures ALTER COLUMN tenant_id SET NOT NULL;
ALTER TABLE skeleton_factures ALTER COLUMN updated_at SET NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_skeleton_facture_tenant_enterprise_code
    ON skeleton_factures(tenant_id, ent_id, fact_code);

CREATE TABLE IF NOT EXISTS purchase_invoice_outbox (
    id BIGSERIAL PRIMARY KEY, event_id VARCHAR(80) NOT NULL, event_type VARCHAR(80) NOT NULL,
    payload TEXT NOT NULL, status VARCHAR(20) NOT NULL, attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITH TIME ZONE NOT NULL, created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    published_at TIMESTAMP WITH TIME ZONE, last_error VARCHAR(500), tenant_id VARCHAR(80) NOT NULL,
    CONSTRAINT uk_purchase_outbox_tenant_event UNIQUE(tenant_id,event_id)
);
CREATE INDEX IF NOT EXISTS ix_purchase_outbox_retry ON purchase_invoice_outbox(status,next_attempt_at,attempts);
