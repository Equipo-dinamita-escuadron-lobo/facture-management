ALTER TABLE skeleton_factures ADD COLUMN IF NOT EXISTS issue_date DATE;
UPDATE skeleton_factures SET issue_date = CAST(created_at AS DATE) WHERE issue_date IS NULL;
