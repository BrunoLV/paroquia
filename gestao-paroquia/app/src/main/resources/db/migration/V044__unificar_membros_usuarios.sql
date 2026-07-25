-- Migration V044: Unificação do conceito de Membro e Usuário (Membro como vínculo de Usuário com Organização)

-- 1. Adiciona os 4 campos sacramentais booleanos na tabela de usuarios
ALTER TABLE calendario.usuarios ADD COLUMN IF NOT EXISTS batizado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE calendario.usuarios ADD COLUMN IF NOT EXISTS primeira_comunhao BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE calendario.usuarios ADD COLUMN IF NOT EXISTS crismado BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE calendario.usuarios ADD COLUMN IF NOT EXISTS casado_igreja_catolica BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Adiciona colunas organizacionais na tabela membros
ALTER TABLE calendario.membros ADD COLUMN IF NOT EXISTS organizacao_id UUID;
ALTER TABLE calendario.membros ADD COLUMN IF NOT EXISTS papel VARCHAR(64) DEFAULT 'MEMBRO';
ALTER TABLE calendario.membros ADD COLUMN IF NOT EXISTS data_entrada DATE DEFAULT CURRENT_DATE;
ALTER TABLE calendario.membros ADD COLUMN IF NOT EXISTS igreja_id UUID;

-- 3. Remove colunas de dados pessoais legadas da tabela membros
ALTER TABLE calendario.membros DROP COLUMN IF EXISTS nome_completo;
ALTER TABLE calendario.membros DROP COLUMN IF EXISTS email;
ALTER TABLE calendario.membros DROP COLUMN IF EXISTS telefone;
ALTER TABLE calendario.membros DROP COLUMN IF EXISTS endereco;
ALTER TABLE calendario.membros DROP COLUMN IF EXISTS data_nascimento;
ALTER TABLE calendario.membros DROP COLUMN IF EXISTS data_batismo;
ALTER TABLE calendario.membros DROP COLUMN IF EXISTS local_batismo;
ALTER TABLE calendario.membros DROP COLUMN IF EXISTS data_crisma;
ALTER TABLE calendario.membros DROP COLUMN IF EXISTS data_matrimonio;

-- 4. Índices de busca e integridade
CREATE INDEX IF NOT EXISTS idx_membros_usuario_id ON calendario.membros (usuario_id);
CREATE INDEX IF NOT EXISTS idx_membros_organizacao_id ON calendario.membros (organizacao_id);
