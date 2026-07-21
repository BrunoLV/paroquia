-- Migration V042: Update role definitions and scope columns for user authorization
ALTER TABLE calendario.membros_organizacao ALTER COLUMN papel TYPE VARCHAR(64);

-- Ensure index exists for role-based lookups
CREATE INDEX IF NOT EXISTS idx_membros_org_papel ON calendario.membros_organizacao (papel);
