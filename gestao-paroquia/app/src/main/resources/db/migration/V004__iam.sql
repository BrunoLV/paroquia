/**
 * MIGRAÇÃO INICIAL: V004__iam.sql
 * CONTEXTO: Cria identidades de acesso, papéis globais e notificações operacionais.
 * REGRA: Credenciais são separadas dos dados civis e os papéis são vinculados a fiéis.
 */

CREATE TABLE calendario.usuarios (
    id uuid NOT NULL,
    username character varying(255) NOT NULL,
    password_hash character varying(255) NOT NULL,
    enabled boolean DEFAULT true NOT NULL,
    version bigint DEFAULT 0 NOT NULL,
    roles character varying(500),
    igreja_id uuid,
    fiel_id uuid,
    criado_em_utc timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    atualizado_em_utc timestamp with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    criado_por character varying(255),
    atualizado_por character varying(255),
    clerigo_id uuid,
    CONSTRAINT ck_usuario_pessoa_exclusiva CHECK ((((fiel_id IS NULL) AND (clerigo_id IS NOT NULL)) OR ((fiel_id IS NOT NULL) AND (clerigo_id IS NULL))))
);
COMMENT ON TABLE calendario.usuarios IS 'Contas de acesso ao sistema (login), distintas do cadastro pastoral de fiel/clérigo a que podem estar vinculadas.';
COMMENT ON COLUMN calendario.usuarios.id IS 'Identificador primário da conta de usuário (UUID).';
COMMENT ON COLUMN calendario.usuarios.username IS 'Login único usado na autenticação.';
COMMENT ON COLUMN calendario.usuarios.password_hash IS 'Hash da senha (nunca texto plano).';
COMMENT ON COLUMN calendario.usuarios.enabled IS 'Indica se a conta está habilitada para login.';
COMMENT ON COLUMN calendario.usuarios.version IS 'Controle de versão para concorrência otimista (JPA).';
COMMENT ON COLUMN calendario.usuarios.roles IS 'Papéis globais atribuídos à conta (admin, bispo-diocesano, paroco, vigario, diacono-paroquial, secretaria-paroquial, fiel), separados do grau de ordem sacramental.';
COMMENT ON COLUMN calendario.usuarios.igreja_id IS 'Unidade territorial (igreja) de referência da conta.';
COMMENT ON COLUMN calendario.usuarios.fiel_id IS 'Fiel associado a esta conta de acesso, quando existente.';
COMMENT ON COLUMN calendario.usuarios.criado_em_utc IS 'Data e hora de criação da conta.';
COMMENT ON COLUMN calendario.usuarios.atualizado_em_utc IS 'Data e hora da última atualização da conta.';
COMMENT ON COLUMN calendario.usuarios.clerigo_id IS 'Clérigo associado a esta conta de acesso, quando existente.';
COMMENT ON COLUMN calendario.usuarios.criado_por IS 'Identificador do responsável por criado.';
COMMENT ON COLUMN calendario.usuarios.atualizado_por IS 'Identificador do responsável por atualizado.';
ALTER TABLE ONLY calendario.usuarios
    ADD CONSTRAINT usuarios_pkey PRIMARY KEY (id);
ALTER TABLE ONLY calendario.usuarios
    ADD CONSTRAINT usuarios_username_key UNIQUE (username);
ALTER TABLE ONLY calendario.usuarios
    ADD CONSTRAINT fk_usuarios_clerigo FOREIGN KEY (clerigo_id) REFERENCES calendario.clerigos(id);
ALTER TABLE ONLY calendario.usuarios
    ADD CONSTRAINT fk_usuarios_fiel FOREIGN KEY (fiel_id) REFERENCES calendario.fieis(id);

CREATE TABLE calendario.papel_global (
    id uuid NOT NULL,
    fiel_id uuid NOT NULL,
    papel character varying(32) NOT NULL,
    ativo boolean DEFAULT true NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    atualizado_em_utc timestamp with time zone NOT NULL,
    criado_por character varying(255),
    atualizado_por character varying(255)
);
COMMENT ON TABLE calendario.papel_global IS 'Papéis globais canônicos (admin, bispo-diocesano, paroco, vigario, diacono-paroquial, secretaria-paroquial, fiel) concedidos a fiéis para funções institucionais transversais.';
COMMENT ON COLUMN calendario.papel_global.id IS 'Identificador único do registro de papel global.';
COMMENT ON COLUMN calendario.papel_global.fiel_id IS 'Identificador do registro relacionado por fiel no contexto de papel global.';
COMMENT ON COLUMN calendario.papel_global.papel IS 'Código canônico do papel global (admin, bispo-diocesano, paroco, vigario, diacono-paroquial, secretaria-paroquial, fiel).';
COMMENT ON COLUMN calendario.papel_global.ativo IS 'Indicador booleano de vigência operacional: ativo.';
COMMENT ON COLUMN calendario.papel_global.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.papel_global.atualizado_em_utc IS 'Instante UTC da última atualização do registro.';
COMMENT ON COLUMN calendario.papel_global.criado_por IS 'Identificador do responsável por criado.';
COMMENT ON COLUMN calendario.papel_global.atualizado_por IS 'Identificador do responsável por atualizado.';
ALTER TABLE ONLY calendario.papel_global
    ADD CONSTRAINT papel_global_pkey PRIMARY KEY (id);
ALTER TABLE ONLY calendario.papel_global
    ADD CONSTRAINT uk_papel_global_fiel_papel UNIQUE (fiel_id, papel);
CREATE INDEX idx_papel_global_fiel_ativo ON calendario.papel_global USING btree (fiel_id, ativo);
ALTER TABLE ONLY calendario.papel_global
    ADD CONSTRAINT fk_papel_global_fiel FOREIGN KEY (fiel_id) REFERENCES calendario.fieis(id);

CREATE TABLE calendario.notificacoes (
    id uuid NOT NULL,
    usuario_id uuid NOT NULL,
    tipo character varying(64) NOT NULL,
    mensagem character varying(500) NOT NULL,
    criado_em_utc timestamp with time zone NOT NULL,
    lida_em_utc timestamp with time zone,
    atualizado_em_utc timestamp with time zone DEFAULT now() NOT NULL,
    criado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL,
    atualizado_por character varying(128) DEFAULT 'sistema'::character varying NOT NULL
);
COMMENT ON TABLE calendario.notificacoes IS 'Mensagens operacionais direcionadas a usuários, com controle de leitura para a central de notificações.';
COMMENT ON COLUMN calendario.notificacoes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da notificacao.';
COMMENT ON COLUMN calendario.notificacoes.criado_por IS 'Identificador do sistema/usuario que gerou a notificacao.';
COMMENT ON COLUMN calendario.notificacoes.atualizado_por IS 'Identificador do usuario/sistema que atualizou a notificacao.';
COMMENT ON COLUMN calendario.notificacoes.id IS 'Identificador único do registro de notificacoes.';
COMMENT ON COLUMN calendario.notificacoes.usuario_id IS 'Identificador do registro relacionado por usuario no contexto de notificacoes.';
COMMENT ON COLUMN calendario.notificacoes.tipo IS 'Propriedade tipo que caracteriza o registro de notificacoes.';
COMMENT ON COLUMN calendario.notificacoes.mensagem IS 'Informação textual de mensagem no contexto de notificacoes.';
COMMENT ON COLUMN calendario.notificacoes.criado_em_utc IS 'Instante UTC de criação imutável do registro.';
COMMENT ON COLUMN calendario.notificacoes.lida_em_utc IS 'Instante UTC associado a lida no ciclo de vida do registro.';
ALTER TABLE ONLY calendario.notificacoes
    ADD CONSTRAINT notificacoes_pkey PRIMARY KEY (id);
CREATE INDEX idx_notificacoes_usuario ON calendario.notificacoes USING btree (usuario_id, lida_em_utc, criado_em_utc DESC);
ALTER TABLE ONLY calendario.notificacoes
    ADD CONSTRAINT notificacoes_usuario_id_fkey FOREIGN KEY (usuario_id) REFERENCES calendario.usuarios(id);


ALTER TABLE ONLY calendario.solicitacoes_ajuste_cadastral
    ADD CONSTRAINT solicitacoes_ajuste_cadastral_analisado_por_usuario_id_fkey FOREIGN KEY (analisado_por_usuario_id) REFERENCES calendario.usuarios(id);
ALTER TABLE ONLY calendario.solicitacoes_ajuste_cadastral
    ADD CONSTRAINT solicitacoes_ajuste_cadastral_usuario_solicitante_id_fkey FOREIGN KEY (usuario_solicitante_id) REFERENCES calendario.usuarios(id);
ALTER TABLE ONLY calendario.solicitacoes_ajuste_clerical
    ADD CONSTRAINT solicitacoes_ajuste_clerical_analisado_por_usuario_id_fkey FOREIGN KEY (analisado_por_usuario_id) REFERENCES calendario.usuarios(id);
ALTER TABLE ONLY calendario.solicitacoes_ajuste_clerical
    ADD CONSTRAINT solicitacoes_ajuste_clerical_usuario_solicitante_id_fkey FOREIGN KEY (usuario_solicitante_id) REFERENCES calendario.usuarios(id);
