/**
 * MIGRAÇÃO: V018__multiplos_locais_evento.sql
 * CONTEXTO: Cria tabela associativa para múltiplos locais por evento e migra dados legados.
 * REGRA: Eventos como retiros ou festas paroquiais podem alocar múltiplos espaços físicos simultaneamente.
 */

CREATE TABLE calendario.eventos_locais (
    evento_id uuid NOT NULL,
    local_id uuid NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);

COMMENT ON TABLE calendario.eventos_locais IS 'Associação entre eventos e múltiplos espaços físicos/locais alocados simultaneamente.';
COMMENT ON COLUMN calendario.eventos_locais.evento_id IS 'Identificador do evento associado.';
COMMENT ON COLUMN calendario.eventos_locais.local_id IS 'Identificador do local/espaço físico alocado.';
COMMENT ON COLUMN calendario.eventos_locais.criado_em_utc IS 'Timestamp UTC da vinculação do local ao evento.';
COMMENT ON COLUMN calendario.eventos_locais.atualizado_em_utc IS 'Timestamp UTC da última atualização da vinculação.';
COMMENT ON COLUMN calendario.eventos_locais.criado_por IS 'Identificador do usuário que realizou a vinculação.';
COMMENT ON COLUMN calendario.eventos_locais.atualizado_por IS 'Identificador do usuário que atualizou a vinculação.';

ALTER TABLE ONLY calendario.eventos_locais
    ADD CONSTRAINT eventos_locais_pkey PRIMARY KEY (evento_id, local_id);

ALTER TABLE ONLY calendario.eventos_locais
    ADD CONSTRAINT fk_eventos_locais_evento FOREIGN KEY (evento_id) REFERENCES calendario.eventos(id) ON DELETE CASCADE;

CREATE INDEX idx_eventos_locais_local_id ON calendario.eventos_locais (local_id);

-- Migração idempotente de dados existentes
INSERT INTO calendario.eventos_locais (evento_id, local_id, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por)
SELECT id, local_id, criado_em_utc, atualizado_em_utc, criado_por, atualizado_por
FROM calendario.eventos
WHERE local_id IS NOT NULL
ON CONFLICT (evento_id, local_id) DO NOTHING;
