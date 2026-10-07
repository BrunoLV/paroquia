-- Mandato de coordenação: encargo temporário, proposto pelo grupo e homologado pelo Pároco (Cân. 519 e 317).
CREATE TABLE calendario.mandato_coordenacao (
    id uuid NOT NULL,
    organizacao_id uuid NOT NULL,
    fiel_id uuid NOT NULL,
    funcao character varying(20) NOT NULL,
    inicio date NOT NULL,
    fim date NOT NULL,
    status character varying(16) NOT NULL,
    homologado_por character varying(128),
    motivo_veto character varying(500),
    decidido_em_utc timestamp with time zone,
    version bigint DEFAULT 0 NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    CONSTRAINT mandato_coordenacao_pkey PRIMARY KEY (id),
    CONSTRAINT mandato_coordenacao_organizacao_fkey FOREIGN KEY (organizacao_id) REFERENCES calendario.organizacao(id),
    CONSTRAINT mandato_coordenacao_fiel_fkey FOREIGN KEY (fiel_id) REFERENCES calendario.fieis(id),
    CONSTRAINT mandato_coordenacao_periodo_ck CHECK (fim > inicio)
);
CREATE INDEX idx_mandato_coordenacao_organizacao ON calendario.mandato_coordenacao USING btree (organizacao_id, status);
CREATE INDEX idx_mandato_coordenacao_vencimento ON calendario.mandato_coordenacao USING btree (status, fim);

COMMENT ON TABLE calendario.mandato_coordenacao IS 'Mandato temporário de coordenação ou vice-coordenação de uma organização, exercido por um fiel após homologação do Pároco.';
COMMENT ON COLUMN calendario.mandato_coordenacao.id IS 'Identificador primário do mandato (UUID).';
COMMENT ON COLUMN calendario.mandato_coordenacao.organizacao_id IS 'Organização cujo mandato de coordenação é exercido.';
COMMENT ON COLUMN calendario.mandato_coordenacao.fiel_id IS 'Fiel que exerce (ou foi proposto para) o encargo.';
COMMENT ON COLUMN calendario.mandato_coordenacao.funcao IS 'Encargo: COORDENADOR ou VICE_COORDENADOR.';
COMMENT ON COLUMN calendario.mandato_coordenacao.inicio IS 'Primeiro dia do mandato.';
COMMENT ON COLUMN calendario.mandato_coordenacao.fim IS 'Último dia do mandato; o encargo é temporário e nunca vitalício.';
COMMENT ON COLUMN calendario.mandato_coordenacao.status IS 'Situação canônica: PROPOSTO, HOMOLOGADO, VETADO ou ENCERRADO.';
COMMENT ON COLUMN calendario.mandato_coordenacao.homologado_por IS 'Papel/identificador de quem homologou ou vetou, sem nome pessoal (LGPD).';
COMMENT ON COLUMN calendario.mandato_coordenacao.motivo_veto IS 'Motivo do veto; obrigatório quando o status é VETADO.';
COMMENT ON COLUMN calendario.mandato_coordenacao.decidido_em_utc IS 'Momento da última decisão (homologação, veto ou encerramento).';
COMMENT ON COLUMN calendario.mandato_coordenacao.version IS 'Controle de versão para concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.mandato_coordenacao.criado_em_utc IS 'Timestamp UTC de criação do registro.';
COMMENT ON COLUMN calendario.mandato_coordenacao.atualizado_em_utc IS 'Timestamp UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.mandato_coordenacao.criado_por IS 'Identificador do usuário que criou o registro.';
COMMENT ON COLUMN calendario.mandato_coordenacao.atualizado_por IS 'Identificador do usuário que atualizou o registro.';

-- Lideranças legadas: membros ativos com papel de coordenação/secretário viram mandatos HOMOLOGADOS de um ano a partir de hoje
-- (histórico anterior não registrava prazo); o Pároco deve revisar e ajustar os períodos.
INSERT INTO calendario.mandato_coordenacao (id, organizacao_id, fiel_id, funcao, inicio, fim, status, homologado_por, decidido_em_utc)
SELECT gen_random_uuid(), m.organizacao_id, m.fiel_id,
       CASE WHEN m.papel = 'coordenador' THEN 'COORDENADOR' ELSE 'VICE_COORDENADOR' END,
       CURRENT_DATE, CURRENT_DATE + INTERVAL '1 year', 'HOMOLOGADO', 'migracao-legado', now()
FROM calendario.membro_grupo m
WHERE m.ativo AND m.papel IN ('coordenador', 'secretario-grupo');
