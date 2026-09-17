/**
 * MIGRAÇÃO INICIAL: V005__membro.sql
 * CONTEXTO: Cria os vínculos de participação entre fiéis, grupos paroquiais e escopos territoriais.
 * REGRA: O papel define a atuação do fiel dentro do grupo e de sua comunidade vinculada.
 */

CREATE TABLE calendario.membro_grupo (
    id uuid NOT NULL,
    fiel_id uuid NOT NULL,
    grupo_paroquial_id uuid NOT NULL,
    tipo_grupo character varying(32) NOT NULL,
    papel character varying(64) NOT NULL,
    igreja_vinculo_id uuid,
    data_entrada date,
    ativo boolean DEFAULT true NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(255),
    atualizado_por character varying(255)
);
COMMENT ON TABLE calendario.membro_grupo IS 'Vínculo de um fiel a grupo paroquial, incluindo papel e escopo territorial de atuação.';
COMMENT ON COLUMN calendario.membro_grupo.id IS 'Identificador único do registro de membro grupo.';
COMMENT ON COLUMN calendario.membro_grupo.fiel_id IS 'Identificador do registro relacionado por fiel no contexto de membro grupo.';
COMMENT ON COLUMN calendario.membro_grupo.grupo_paroquial_id IS 'Identificador do registro relacionado por grupo paroquial no contexto de membro grupo.';
COMMENT ON COLUMN calendario.membro_grupo.tipo_grupo IS 'Propriedade tipo grupo que caracteriza o registro de membro grupo.';
COMMENT ON COLUMN calendario.membro_grupo.papel IS 'Propriedade papel que caracteriza o registro de membro grupo.';
COMMENT ON COLUMN calendario.membro_grupo.igreja_vinculo_id IS 'Identificador do registro relacionado por igreja vinculo no contexto de membro grupo.';
COMMENT ON COLUMN calendario.membro_grupo.data_entrada IS 'Data de negócio referente a entrada.';
COMMENT ON COLUMN calendario.membro_grupo.ativo IS 'Indicador booleano de vigência operacional: ativo.';
COMMENT ON COLUMN calendario.membro_grupo.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.membro_grupo.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.membro_grupo.criado_por IS 'Identificador do responsável por criado.';
COMMENT ON COLUMN calendario.membro_grupo.atualizado_por IS 'Identificador do responsável por atualizado.';
ALTER TABLE ONLY calendario.membro_grupo
    ADD CONSTRAINT membro_grupo_pkey PRIMARY KEY (id);
CREATE INDEX idx_membro_grupo_fiel_ativo ON calendario.membro_grupo USING btree (fiel_id, ativo);
CREATE INDEX idx_membro_grupo_igreja_vinculo_ativo ON calendario.membro_grupo USING btree (igreja_vinculo_id, ativo);
CREATE INDEX idx_membro_grupo_organizacao_ativo ON calendario.membro_grupo USING btree (grupo_paroquial_id, ativo);
ALTER TABLE ONLY calendario.membro_grupo
    ADD CONSTRAINT fk_membro_grupo_fiel FOREIGN KEY (fiel_id) REFERENCES calendario.fieis(id);


