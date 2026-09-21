/**
 * MIGRAÇÃO: V016__aprovacao_recorrencia_e_conflitos.sql
 * CONTEXTO: Suporte a modelo mestre-detalhe para recorrências de eventos e armazenamento
 *           analítico de conflitos detectados para deliberação do Conselho/Pároco.
 */

ALTER TABLE calendario.aprovacoes
    ADD COLUMN IF NOT EXISTS aprovacao_pai_id uuid,
    ADD COLUMN IF NOT EXISTS conflitos_json text,
    ADD COLUMN IF NOT EXISTS data_ocorrencia_utc timestamp with time zone,
    ADD COLUMN IF NOT EXISTS justificativa character varying(2000);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_aprovacoes_pai'
    ) THEN
        ALTER TABLE calendario.aprovacoes
            ADD CONSTRAINT fk_aprovacoes_pai
            FOREIGN KEY (aprovacao_pai_id)
            REFERENCES calendario.aprovacoes(id)
            ON DELETE CASCADE;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_aprovacoes_pai_id ON calendario.aprovacoes USING btree (aprovacao_pai_id);
CREATE INDEX IF NOT EXISTS idx_aprovacoes_data_ocorrencia ON calendario.aprovacoes USING btree (data_ocorrencia_utc);

COMMENT ON COLUMN calendario.aprovacoes.aprovacao_pai_id IS 'Identificador da aprovação mestre agregadora quando se tratar de ocorrência individual de uma série recorrente.';
COMMENT ON COLUMN calendario.aprovacoes.conflitos_json IS 'Estrutura JSON com o detalhamento analítico de conflitos de locais, ativos ou prioridades eclesiais para deliberação.';
COMMENT ON COLUMN calendario.aprovacoes.data_ocorrencia_utc IS 'Instante UTC da ocorrência programada associada à solicitação de aprovação.';
COMMENT ON COLUMN calendario.aprovacoes.justificativa IS 'Justificativa fornecida pelo solicitante para embasamento da decisão administrativa e pastoral.';
