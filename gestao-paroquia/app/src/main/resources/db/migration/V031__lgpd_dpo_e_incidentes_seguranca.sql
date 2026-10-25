/**
 * MIGRAÇÃO EVOLUTIVA: V031__lgpd_dpo_e_incidentes_seguranca.sql
 * CONTEXTO: Conformidade com a LGPD (Lei 13.709/2018) - Dados do Encarregado (DPO) e Registro de Incidentes (Art. 48).
 * REGRA: A paróquia mantém publicamente os canais de contato do DPO institucional e registra formalmente
 * incidentes de segurança cibernética para fins probatórios e comunicação à ANPD e aos titulares.
 */

-- 1. Canais do Encarregado de Proteção de Dados (DPO) na configuração paroquial
ALTER TABLE calendario.paroquia_configuracao
    ADD COLUMN IF NOT EXISTS dpo_nome character varying(150),
    ADD COLUMN IF NOT EXISTS dpo_email character varying(150),
    ADD COLUMN IF NOT EXISTS dpo_telefone character varying(20);

COMMENT ON COLUMN calendario.paroquia_configuracao.dpo_nome IS 'Nome formal do Encarregado de Proteção de Dados (DPO) da paróquia.';
COMMENT ON COLUMN calendario.paroquia_configuracao.dpo_email IS 'E-mail institucional oficial do DPO para contato com titulares e ANPD.';
COMMENT ON COLUMN calendario.paroquia_configuracao.dpo_telefone IS 'Telefone institucional de atendimento do DPO.';

-- 2. Tabela de Registro de Incidentes de Segurança (Art. 48)
CREATE TABLE IF NOT EXISTS calendario.incidentes_seguranca (
    id uuid NOT NULL,
    titulo character varying(200) NOT NULL,
    descricao text NOT NULL,
    tipo_dado_afetado character varying(100) NOT NULL,
    categoria_gravidade character varying(30) NOT NULL,
    data_ocorrencia timestamp with time zone NOT NULL,
    data_deteccao timestamp with time zone NOT NULL,
    medidas_mitigacao text,
    notificado_anpd boolean DEFAULT false NOT NULL,
    data_notificacao_anpd timestamp with time zone,
    protocolo_anpd character varying(100),
    notificado_titulares boolean DEFAULT false NOT NULL,
    data_notificacao_titulares timestamp with time zone,
    status character varying(30) DEFAULT 'EM_ANALISE' NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema' NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema' NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    CONSTRAINT incidentes_seguranca_pkey PRIMARY KEY (id)
);

COMMENT ON TABLE calendario.incidentes_seguranca IS 'Registro formal de incidentes de segurança e violações de dados pessoais (Art. 48 LGPD).';
COMMENT ON COLUMN calendario.incidentes_seguranca.id IS 'Identificador único do registro de incidente de segurança.';
COMMENT ON COLUMN calendario.incidentes_seguranca.titulo IS 'Título resumido do incidente de segurança.';
COMMENT ON COLUMN calendario.incidentes_seguranca.descricao IS 'Descrição detalhada do incidente e sistemas/processos impactados.';
COMMENT ON COLUMN calendario.incidentes_seguranca.tipo_dado_afetado IS 'Natureza dos dados pessoais afetados (ex.: cadastrais civis, dados sensíveis sacramentais).';
COMMENT ON COLUMN calendario.incidentes_seguranca.categoria_gravidade IS 'Gravidade da ocorrência: BAIXA, MEDIA, ALTA, CRITICA.';
COMMENT ON COLUMN calendario.incidentes_seguranca.data_ocorrencia IS 'Data e hora presumida da ocorrência do incidente.';
COMMENT ON COLUMN calendario.incidentes_seguranca.data_deteccao IS 'Data e hora da detecção formal da violação pela paróquia.';
COMMENT ON COLUMN calendario.incidentes_seguranca.medidas_mitigacao IS 'Ações técnicas e administrativas de contenção e remediação adotadas.';
COMMENT ON COLUMN calendario.incidentes_seguranca.notificado_anpd IS 'Indicador se a ANPD foi comunicada tempestivamente.';
COMMENT ON COLUMN calendario.incidentes_seguranca.data_notificacao_anpd IS 'Data e hora do envio da comunicação formal à ANPD.';
COMMENT ON COLUMN calendario.incidentes_seguranca.protocolo_anpd IS 'Número do protocolo de atendimento ou processo na ANPD.';
COMMENT ON COLUMN calendario.incidentes_seguranca.notificado_titulares IS 'Indicador se os titulares afetados foram comunicados diretamente.';
COMMENT ON COLUMN calendario.incidentes_seguranca.data_notificacao_titulares IS 'Data e hora da comunicação direcionada aos titulares.';
COMMENT ON COLUMN calendario.incidentes_seguranca.status IS 'Estado atual do tratamento do incidente: EM_ANALISE, CONTIDO, NOTIFICADO, ENCERRADO.';
COMMENT ON COLUMN calendario.incidentes_seguranca.criado_em_utc IS 'Timestamp UTC imutável de criação do registro de incidente.';
COMMENT ON COLUMN calendario.incidentes_seguranca.atualizado_em_utc IS 'Timestamp UTC da última alteração do incidente.';
COMMENT ON COLUMN calendario.incidentes_seguranca.criado_por IS 'Identificador do usuário que registrou o incidente.';
COMMENT ON COLUMN calendario.incidentes_seguranca.atualizado_por IS 'Identificador do usuário que realizou a última atualização.';
COMMENT ON COLUMN calendario.incidentes_seguranca.version IS 'Controle de concorrência otimista (JPA @Version).';
