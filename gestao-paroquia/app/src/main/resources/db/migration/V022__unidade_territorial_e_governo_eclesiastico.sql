/**
 * MIGRAÇÃO: V022__unidade_territorial_e_governo_eclesiastico.sql
 * CONTEXTO: Separação Canônica entre Unidade Territorial (física: MATRIZ, COMUNIDADE) e Governo Eclesiástico (Diocese, Pároco).
 * PRIVACIDADE: Dados PII de contato e endereço físico são protegidos por criptografia em repouso (LGPD).
 * REGRA: Não armazena chave PIX (minimização). Unidade Territorial restrita a MATRIZ e COMUNIDADE.
 */

-- 1. Tabela de Dioceses
CREATE TABLE calendario.dioceses (
    id uuid NOT NULL,
    nome character varying(180) NOT NULL,
    bispo_diocesano character varying(180) NOT NULL,
    cnpj_mitra character varying(18),
    cidade character varying(100),
    uf character varying(2),
    contato character varying(500),
    ativo boolean DEFAULT true NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.dioceses IS 'Igrejas Particulares (Dioceses/Arquidioceses) às quais as paróquias estão canonicamente circunscritas.';
COMMENT ON COLUMN calendario.dioceses.id IS 'Identificador único da diocese (UUID).';
COMMENT ON COLUMN calendario.dioceses.nome IS 'Nome canônico da diocese ou arquidiocese.';
COMMENT ON COLUMN calendario.dioceses.bispo_diocesano IS 'Nome eclesiástico do bispo diocesano ou arcebispo titular.';
COMMENT ON COLUMN calendario.dioceses.cnpj_mitra IS 'CNPJ da Mitra Diocesana titular perante a Receita Federal.';
COMMENT ON COLUMN calendario.dioceses.cidade IS 'Município sede da cúria diocesana.';
COMMENT ON COLUMN calendario.dioceses.uf IS 'Unidade Federativa da sede diocesana (sigla de 2 caracteres).';
COMMENT ON COLUMN calendario.dioceses.contato IS 'Contatos da cúria diocesana (telefone/e-mail, criptografado em repouso).';
COMMENT ON COLUMN calendario.dioceses.ativo IS 'Indicador de vigência e atividade da circunscrição eclesiástica.';
COMMENT ON COLUMN calendario.dioceses.version IS 'Controle de concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.dioceses.criado_em_utc IS 'Timestamp UTC de criação gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.dioceses.atualizado_em_utc IS 'Timestamp UTC de atualização gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.dioceses.criado_por IS 'Identificador do usuário que registrou a diocese.';
COMMENT ON COLUMN calendario.dioceses.atualizado_por IS 'Identificador do usuário que atualizou a diocese.';
ALTER TABLE ONLY calendario.dioceses
    ADD CONSTRAINT dioceses_pkey PRIMARY KEY (id);
ALTER TABLE ONLY calendario.dioceses
    ADD CONSTRAINT uk_dioceses_nome UNIQUE (nome);

-- 2. Tabela de Governo Eclesiástico Paroquial
CREATE TABLE calendario.governo_eclesiastico (
    id uuid NOT NULL,
    diocese_id uuid NOT NULL,
    paroco_id uuid,
    data_posse_paroco date,
    decreto_erecao_paroquia character varying(100),
    data_instalacao_paroquia date,
    livro_tombo_numero character varying(50),
    version bigint DEFAULT 0 NOT NULL,
    criado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.governo_eclesiastico IS 'Estrutura institucional de governo e provisão canônica da paróquia perante sua diocese e o clero.';
COMMENT ON COLUMN calendario.governo_eclesiastico.id IS 'Identificador único do registro de governo eclesiástico.';
COMMENT ON COLUMN calendario.governo_eclesiastico.diocese_id IS 'Diocese à qual a paróquia pertence canonicamente.';
COMMENT ON COLUMN calendario.governo_eclesiastico.paroco_id IS 'Clérigo em exercício com encargo de Pároco (administrador geral nato da paróquia).';
COMMENT ON COLUMN calendario.governo_eclesiastico.data_posse_paroco IS 'Data canônica da tomada de posse do pároco atual.';
COMMENT ON COLUMN calendario.governo_eclesiastico.decreto_erecao_paroquia IS 'Número ou identificador do decreto episcopal de ereção canônica da paróquia.';
COMMENT ON COLUMN calendario.governo_eclesiastico.data_instalacao_paroquia IS 'Data solene de ereção e instalação da paróquia na diocese.';
COMMENT ON COLUMN calendario.governo_eclesiastico.livro_tombo_numero IS 'Número do livro e página do arquivo paroquial referente à fundação/atos solenes.';
COMMENT ON COLUMN calendario.governo_eclesiastico.version IS 'Controle de versão otimista JPA.';
COMMENT ON COLUMN calendario.governo_eclesiastico.criado_em_utc IS 'Timestamp UTC de criação gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.governo_eclesiastico.atualizado_em_utc IS 'Timestamp UTC de atualização gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.governo_eclesiastico.criado_por IS 'Identificador do usuário que registrou o governo eclesiástico.';
COMMENT ON COLUMN calendario.governo_eclesiastico.atualizado_por IS 'Identificador do usuário que atualizou o governo eclesiástico.';
ALTER TABLE ONLY calendario.governo_eclesiastico
    ADD CONSTRAINT governo_eclesiastico_pkey PRIMARY KEY (id);
ALTER TABLE ONLY calendario.governo_eclesiastico
    ADD CONSTRAINT fk_governo_diocese FOREIGN KEY (diocese_id) REFERENCES calendario.dioceses(id);
ALTER TABLE ONLY calendario.governo_eclesiastico
    ADD CONSTRAINT fk_governo_paroco FOREIGN KEY (paroco_id) REFERENCES calendario.clerigos(id);

-- 3. Limpeza e migração segura de dados legados do tipo CLERO em unidade_territorial
UPDATE calendario.encargos_clericais
SET unidade_territorial_id = (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ' LIMIT 1)
WHERE unidade_territorial_id IN (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'CLERO')
  AND EXISTS (SELECT 1 FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ');

UPDATE calendario.unidade_territorial
SET parent_id = NULL
WHERE parent_id IN (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'CLERO');

UPDATE calendario.locais
SET grupo_paroquial_id = (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ' LIMIT 1)
WHERE grupo_paroquial_id IN (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'CLERO')
  AND EXISTS (SELECT 1 FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ');

UPDATE calendario.eventos
SET igreja_vinculo_id = (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ' LIMIT 1)
WHERE igreja_vinculo_id IN (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'CLERO')
  AND EXISTS (SELECT 1 FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ');

UPDATE calendario.membro_grupo
SET igreja_vinculo_id = (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ' LIMIT 1)
WHERE igreja_vinculo_id IN (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'CLERO')
  AND EXISTS (SELECT 1 FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ');

UPDATE calendario.grupo_paroquial
SET parent_id = (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ' LIMIT 1)
WHERE parent_id IN (SELECT id FROM calendario.unidade_territorial WHERE tipo = 'CLERO')
  AND EXISTS (SELECT 1 FROM calendario.unidade_territorial WHERE tipo = 'MATRIZ');

DELETE FROM calendario.unidade_territorial WHERE tipo = 'CLERO';

-- 4. Constraint de restrição de tipo para MATRIZ e COMUNIDADE
ALTER TABLE ONLY calendario.unidade_territorial
    ADD CONSTRAINT ck_unidade_territorial_tipo CHECK (tipo IN ('MATRIZ', 'COMUNIDADE'));

-- 5. Expansão de colunas em unidade_territorial
ALTER TABLE calendario.unidade_territorial
    -- Canônico
    ADD COLUMN IF NOT EXISTS padroeiro character varying(150),
    ADD COLUMN IF NOT EXISTS data_festa_padroeira character varying(5),
    ADD COLUMN IF NOT EXISTS data_dedicacao_altar date,
    ADD COLUMN IF NOT EXISTS possui_sacrario boolean DEFAULT true NOT NULL,
    ADD COLUMN IF NOT EXISTS possui_pia_batismal boolean DEFAULT false NOT NULL,
    ADD COLUMN IF NOT EXISTS clerigo_responsavel_id uuid,
    
    -- Fiscal e Jurídico
    ADD COLUMN IF NOT EXISTS cnpj character varying(18),
    ADD COLUMN IF NOT EXISTS inscricao_municipal character varying(30),
    ADD COLUMN IF NOT EXISTS inscricao_imobiliaria_iptu character varying(50),
    ADD COLUMN IF NOT EXISTS processo_imunidade_iptu character varying(50),
    
    -- Registro de Imóveis (RGI)
    ADD COLUMN IF NOT EXISTS cartorio_rgi character varying(120),
    ADD COLUMN IF NOT EXISTS matricula_imovel character varying(50),
    ADD COLUMN IF NOT EXISTS tipo_aquisicao character varying(30),
    ADD COLUMN IF NOT EXISTS area_terreno_m2 numeric(10,2),
    ADD COLUMN IF NOT EXISTS area_construida_m2 numeric(10,2),
    
    -- Regularidade e Bombeiros
    ADD COLUMN IF NOT EXISTS avcb_numero character varying(50),
    ADD COLUMN IF NOT EXISTS avcb_validade date,
    ADD COLUMN IF NOT EXISTS lotacao_maxima_bombeiros integer,
    
    -- Concessionárias
    ADD COLUMN IF NOT EXISTS codigo_consumidor_energia character varying(50),
    ADD COLUMN IF NOT EXISTS codigo_consumidor_agua character varying(50),
    
    -- PII Protegido por Criptografia em Repouso (LGPD)
    ADD COLUMN IF NOT EXISTS telefone character varying(500),
    ADD COLUMN IF NOT EXISTS email character varying(500),
    ADD COLUMN IF NOT EXISTS endereco_cep character varying(255),
    ADD COLUMN IF NOT EXISTS endereco_logradouro character varying(500),
    ADD COLUMN IF NOT EXISTS endereco_numero character varying(255),
    ADD COLUMN IF NOT EXISTS endereco_complemento character varying(255),
    ADD COLUMN IF NOT EXISTS endereco_bairro character varying(255),
    ADD COLUMN IF NOT EXISTS endereco_cidade character varying(255),
    ADD COLUMN IF NOT EXISTS endereco_uf character varying(2);

ALTER TABLE ONLY calendario.unidade_territorial
    ADD CONSTRAINT fk_unidade_clerigo_responsavel FOREIGN KEY (clerigo_responsavel_id) REFERENCES calendario.clerigos(id);

COMMENT ON COLUMN calendario.unidade_territorial.padroeiro IS 'Santo padroeiro ou orago canônico da matriz ou capela.';
COMMENT ON COLUMN calendario.unidade_territorial.data_festa_padroeira IS 'Dia e mês da festa patronal no formato MM-DD.';
COMMENT ON COLUMN calendario.unidade_territorial.data_dedicacao_altar IS 'Data canônica da solene bênção ou dedicação da igreja/altar.';
COMMENT ON COLUMN calendario.unidade_territorial.possui_sacrario IS 'Indicador de presença da reserva do Santíssimo Sacramento.';
COMMENT ON COLUMN calendario.unidade_territorial.possui_pia_batismal IS 'Indicador de fonte batismal canônica (obrigatória na Matriz; excepcional em capelas).';
COMMENT ON COLUMN calendario.unidade_territorial.clerigo_responsavel_id IS 'Clérigo diretamente encarregado da assistência pastoral da comunidade (se aplicável).';
COMMENT ON COLUMN calendario.unidade_territorial.cnpj IS 'CNPJ filial ou de referência faturado para a comunidade.';
COMMENT ON COLUMN calendario.unidade_territorial.inscricao_municipal IS 'Inscrição municipal (CCM) do estabelecimento perante a prefeitura.';
COMMENT ON COLUMN calendario.unidade_territorial.inscricao_imobiliaria_iptu IS 'Inscrição cadastral do imóvel na prefeitura para IPTU.';
COMMENT ON COLUMN calendario.unidade_territorial.processo_imunidade_iptu IS 'Número do processo administrativo de imunidade tributária religiosa do imóvel.';
COMMENT ON COLUMN calendario.unidade_territorial.cartorio_rgi IS 'Serventia registral imobiliária de circunscrição do imóvel.';
COMMENT ON COLUMN calendario.unidade_territorial.matricula_imovel IS 'Número de matrícula ou transcrição imobiliária no RGI.';
COMMENT ON COLUMN calendario.unidade_territorial.tipo_aquisicao IS 'Modalidade aquisitiva do solo (DOACAO, COMPRA_E_VENDA, COMODATO, etc.).';
COMMENT ON COLUMN calendario.unidade_territorial.area_terreno_m2 IS 'Área total do lote/terreno em metros quadrados.';
COMMENT ON COLUMN calendario.unidade_territorial.area_construida_m2 IS 'Área construída total da edificação em metros quadrados.';
COMMENT ON COLUMN calendario.unidade_territorial.avcb_numero IS 'Número do laudo do Corpo de Bombeiros (AVCB/CLCB).';
COMMENT ON COLUMN calendario.unidade_territorial.avcb_validade IS 'Data de expiração/vencimento do laudo do Corpo de Bombeiros.';
COMMENT ON COLUMN calendario.unidade_territorial.lotacao_maxima_bombeiros IS 'Lotação máxima de público autorizada pelo Corpo de Bombeiros.';
COMMENT ON COLUMN calendario.unidade_territorial.codigo_consumidor_energia IS 'Código da instalação / unidade consumidora na concessionária de energia elétrica.';
COMMENT ON COLUMN calendario.unidade_territorial.codigo_consumidor_agua IS 'Número de ligação / hidrômetro na concessionária de água e saneamento.';
COMMENT ON COLUMN calendario.unidade_territorial.telefone IS 'Telefone de contato da secretaria da unidade (criptografado em repouso).';
COMMENT ON COLUMN calendario.unidade_territorial.email IS 'E-mail de contato da unidade (criptografado em repouso).';
COMMENT ON COLUMN calendario.unidade_territorial.endereco_cep IS 'CEP do imóvel da unidade territorial (criptografado em repouso).';
COMMENT ON COLUMN calendario.unidade_territorial.endereco_logradouro IS 'Logradouro do imóvel da unidade territorial (criptografado em repouso).';
COMMENT ON COLUMN calendario.unidade_territorial.endereco_numero IS 'Número predial do imóvel da unidade territorial (criptografado em repouso).';
COMMENT ON COLUMN calendario.unidade_territorial.endereco_complemento IS 'Complemento do imóvel da unidade territorial (criptografado em repouso).';
COMMENT ON COLUMN calendario.unidade_territorial.endereco_bairro IS 'Bairro do imóvel da unidade territorial (criptografado em repouso).';
COMMENT ON COLUMN calendario.unidade_territorial.endereco_cidade IS 'Município sede da unidade territorial (criptografado em repouso).';
COMMENT ON COLUMN calendario.unidade_territorial.endereco_uf IS 'Unidade Federativa da unidade territorial (2 caracteres).';

-- 6. Inserção de registro diocesano inicial padrão para permitir vinculação consistente
INSERT INTO calendario.dioceses (id, nome, bispo_diocesano, cnpj_mitra, cidade, uf, contato, ativo, version, criado_por, atualizado_por)
VALUES (
    '00000000-0000-0000-0000-000000000101',
    'Diocese de São Miguel Paulista',
    'Dom Manuel Parrado Carral (Emérito) / Dom Algacir Munhak',
    '61.401.765/0001-38',
    'São Paulo',
    'SP',
    NULL,
    true,
    0,
    'sistema',
    'sistema'
) ON CONFLICT (nome) DO NOTHING;
