-- V087: Padronizacao universal de auditoria de entidades via Spring Data JPA.
-- Adiciona e padroniza as 4 colunas em todas as tabelas mapeadas:
-- criado_em_utc (TIMESTAMPTZ), atualizado_em_utc (TIMESTAMPTZ), criado_por (VARCHAR(128)), atualizado_por (VARCHAR(128))

-- 1. Tabelas com colunas historicas em ingles (created_at / updated_at):
-- locais
ALTER TABLE calendario.locais RENAME COLUMN created_at TO criado_em_utc;
ALTER TABLE calendario.locais RENAME COLUMN updated_at TO atualizado_em_utc;
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.locais ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.locais.criado_em_utc IS 'Timestamp UTC de criacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.locais.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.locais.criado_por IS 'Identificador do usuario/sistema que criou o registro.';
COMMENT ON COLUMN calendario.locais.atualizado_por IS 'Identificador do usuario/sistema que realizou a ultima modificacao.';

-- reservas
ALTER TABLE calendario.reservas RENAME COLUMN created_at TO criado_em_utc;
ALTER TABLE calendario.reservas RENAME COLUMN updated_at TO atualizado_em_utc;
ALTER TABLE calendario.reservas ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.reservas ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.reservas.criado_em_utc IS 'Timestamp UTC de criacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.reservas.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.reservas.criado_por IS 'Identificador do usuario/sistema que criou a reserva.';
COMMENT ON COLUMN calendario.reservas.atualizado_por IS 'Identificador do usuario/sistema que modificou a reserva.';

-- ativos_patrimoniais
ALTER TABLE calendario.ativos_patrimoniais RENAME COLUMN updated_at TO atualizado_em_utc;
ALTER TABLE calendario.ativos_patrimoniais ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.ativos_patrimoniais ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.ativos_patrimoniais.criado_em_utc IS 'Timestamp UTC de criacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.criado_por IS 'Identificador do usuario/sistema que cadastrou o ativo patrimonial.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.atualizado_por IS 'Identificador do usuario/sistema que realizou a ultima modificacao.';

-- ativo_manutencoes
ALTER TABLE calendario.ativo_manutencoes RENAME COLUMN created_at TO criado_em_utc;
ALTER TABLE calendario.ativo_manutencoes RENAME COLUMN updated_at TO atualizado_em_utc;
ALTER TABLE calendario.ativo_manutencoes ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.ativo_manutencoes ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.ativo_manutencoes.criado_em_utc IS 'Timestamp UTC de criacao do registro gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.ativo_manutencoes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da manutencao de ativo.';
COMMENT ON COLUMN calendario.ativo_manutencoes.criado_por IS 'Identificador do usuario que abriu a manutencao.';
COMMENT ON COLUMN calendario.ativo_manutencoes.atualizado_por IS 'Identificador do usuario que atualizou a manutencao.';

-- evento_idempotency
ALTER TABLE calendario.evento_idempotency RENAME COLUMN created_at TO criado_em_utc;
ALTER TABLE calendario.evento_idempotency ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.evento_idempotency ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.evento_idempotency ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.evento_idempotency.criado_em_utc IS 'Timestamp UTC de criacao do token de idempotencia gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.evento_idempotency.atualizado_em_utc IS 'Timestamp UTC de atualizacao do token de idempotencia.';
COMMENT ON COLUMN calendario.evento_idempotency.criado_por IS 'Identificador do criador do registro de idempotencia.';
COMMENT ON COLUMN calendario.evento_idempotency.atualizado_por IS 'Identificador do atualizador do registro de idempotencia.';

-- anos_paroquiais
ALTER TABLE calendario.anos_paroquiais RENAME COLUMN updated_at TO atualizado_em_utc;
ALTER TABLE calendario.anos_paroquiais ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.anos_paroquiais ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.anos_paroquiais ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.anos_paroquiais.criado_em_utc IS 'Timestamp UTC de abertura do ano paroquial.';
COMMENT ON COLUMN calendario.anos_paroquiais.atualizado_em_utc IS 'Timestamp UTC da ultima alteracao no ano paroquial.';
COMMENT ON COLUMN calendario.anos_paroquiais.criado_por IS 'Identificador do usuario que abriu o ano.';
COMMENT ON COLUMN calendario.anos_paroquiais.atualizado_por IS 'Identificador do usuario que modificou o ano.';

-- 2. Tabelas com apenas criado_em_utc:
-- aprovacoes
ALTER TABLE calendario.aprovacoes ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.aprovacoes ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.aprovacoes ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.aprovacoes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.aprovacoes.criado_por IS 'Identificador do usuario que criou a solicitacao de aprovacao.';
COMMENT ON COLUMN calendario.aprovacoes.atualizado_por IS 'Identificador do usuario que realizou a ultima alteracao no workflow.';

-- cpc_decisoes
ALTER TABLE calendario.cpc_decisoes ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.cpc_decisoes ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.cpc_decisoes ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.cpc_decisoes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.cpc_decisoes.criado_por IS 'Identificador do solicitante/sistema que registrou a decisao do CPC.';
COMMENT ON COLUMN calendario.cpc_decisoes.atualizado_por IS 'Identificador do usuario que realizou a ultima alteracao na decisao.';

-- notificacoes
ALTER TABLE calendario.notificacoes ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.notificacoes ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.notificacoes ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.notificacoes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da notificacao.';
COMMENT ON COLUMN calendario.notificacoes.criado_por IS 'Identificador do sistema/usuario que gerou a notificacao.';
COMMENT ON COLUMN calendario.notificacoes.atualizado_por IS 'Identificador do usuario/sistema que atualizou a notificacao.';

-- encargos_clericais
ALTER TABLE calendario.encargos_clericais ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.encargos_clericais ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.encargos_clericais ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.encargos_clericais.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do encargo clerical.';
COMMENT ON COLUMN calendario.encargos_clericais.criado_por IS 'Identificador do usuario/sistema que registrou o encargo clerical.';
COMMENT ON COLUMN calendario.encargos_clericais.atualizado_por IS 'Identificador do usuario que atualizou o encargo clerical.';

-- solicitacoes_ajuste_cadastral
ALTER TABLE calendario.solicitacoes_ajuste_cadastral ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.solicitacoes_ajuste_cadastral ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.solicitacoes_ajuste_cadastral ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da solicitacao de ajuste cadastral.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.criado_por IS 'Identificador do solicitante/sistema.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_cadastral.atualizado_por IS 'Identificador do revisor/sistema.';

-- solicitacoes_ajuste_clerical
ALTER TABLE calendario.solicitacoes_ajuste_clerical ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.solicitacoes_ajuste_clerical ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.solicitacoes_ajuste_clerical ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da solicitacao de ajuste clerical.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.criado_por IS 'Identificador do solicitante/sistema.';
COMMENT ON COLUMN calendario.solicitacoes_ajuste_clerical.atualizado_por IS 'Identificador do revisor/sistema.';

-- projeto_solicitacoes_aprovacao
ALTER TABLE calendario.projeto_solicitacoes_aprovacao ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.projeto_solicitacoes_aprovacao ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.projeto_solicitacoes_aprovacao ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da aprovacao de projeto.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.criado_por IS 'Identificador do solicitante.';
COMMENT ON COLUMN calendario.projeto_solicitacoes_aprovacao.atualizado_por IS 'Identificador do aprovador.';

-- relacionamentos_familiares
ALTER TABLE calendario.relacionamentos_familiares ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.relacionamentos_familiares ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.relacionamentos_familiares ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.relacionamentos_familiares.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do relacionamento familiar.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.criado_por IS 'Identificador do usuario que registrou o relacionamento.';
COMMENT ON COLUMN calendario.relacionamentos_familiares.atualizado_por IS 'Identificador do usuario que atualizou o relacionamento.';

-- fiel_relacionamentos_familiares
ALTER TABLE calendario.fiel_relacionamentos_familiares ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.fiel_relacionamentos_familiares ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.fiel_relacionamentos_familiares ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.fiel_relacionamentos_familiares.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do vinculo familiar do fiel.';
COMMENT ON COLUMN calendario.fiel_relacionamentos_familiares.criado_por IS 'Identificador do usuario que registrou o vinculo.';
COMMENT ON COLUMN calendario.fiel_relacionamentos_familiares.atualizado_por IS 'Identificador do usuario que atualizou o vinculo.';

-- fiel_dependentes
ALTER TABLE calendario.fiel_dependentes ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.fiel_dependentes ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.fiel_dependentes ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.fiel_dependentes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do dependente.';
COMMENT ON COLUMN calendario.fiel_dependentes.criado_por IS 'Identificador do usuario que registrou o dependente.';
COMMENT ON COLUMN calendario.fiel_dependentes.atualizado_por IS 'Identificador do usuario que atualizou o dependente.';

-- observacoes_evento
ALTER TABLE calendario.observacoes_evento ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.observacoes_evento ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.observacoes_evento ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.observacoes_evento.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da observacao do evento.';
COMMENT ON COLUMN calendario.observacoes_evento.criado_por IS 'Identificador do autor da observacao.';
COMMENT ON COLUMN calendario.observacoes_evento.atualizado_por IS 'Identificador do ultimo usuario a modificar a observacao.';

-- observacoes_ativo_patrimonial
ALTER TABLE calendario.observacoes_ativo_patrimonial ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.observacoes_ativo_patrimonial ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.observacoes_ativo_patrimonial ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da observacao patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.criado_por IS 'Identificador do autor da observacao patrimonial.';
COMMENT ON COLUMN calendario.observacoes_ativo_patrimonial.atualizado_por IS 'Identificador do usuario que atualizou a observacao patrimonial.';

-- 3. Tabelas sem colunas de auditoria:
-- eventos
ALTER TABLE calendario.eventos ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.eventos ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.eventos ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.eventos ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.eventos.criado_em_utc IS 'Timestamp UTC de criacao do evento gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.eventos.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do evento gerenciado pelo Spring Data JPA Auditing.';
COMMENT ON COLUMN calendario.eventos.criado_por IS 'Identificador do usuario/sistema que criou o evento.';
COMMENT ON COLUMN calendario.eventos.atualizado_por IS 'Identificador do usuario/sistema que modificou o evento.';

-- eventos_envolvidos
ALTER TABLE calendario.eventos_envolvidos ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.eventos_envolvidos ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.eventos_envolvidos ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.eventos_envolvidos ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.eventos_envolvidos.criado_em_utc IS 'Timestamp UTC de associacao da organizacao ao evento.';
COMMENT ON COLUMN calendario.eventos_envolvidos.atualizado_em_utc IS 'Timestamp UTC de modificacao da participacao.';
COMMENT ON COLUMN calendario.eventos_envolvidos.criado_por IS 'Identificador do usuario que associou a organizacao.';
COMMENT ON COLUMN calendario.eventos_envolvidos.atualizado_por IS 'Identificador do usuario que atualizou a participacao.';

-- eventos_recorrencia
ALTER TABLE calendario.eventos_recorrencia ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.eventos_recorrencia ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.eventos_recorrencia ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.eventos_recorrencia ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.eventos_recorrencia.criado_em_utc IS 'Timestamp UTC de criacao da regra de recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.criado_por IS 'Identificador do usuario que criou a recorrencia.';
COMMENT ON COLUMN calendario.eventos_recorrencia.atualizado_por IS 'Identificador do usuario que modificou a recorrencia.';

-- observacoes_nota_revisoes
ALTER TABLE calendario.observacoes_nota_revisoes ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.observacoes_nota_revisoes ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.observacoes_nota_revisoes ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.observacoes_nota_revisoes ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.observacoes_nota_revisoes.criado_em_utc IS 'Timestamp UTC da revisao de observacao.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao da revisao.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.criado_por IS 'Identificador do autor da revisao.';
COMMENT ON COLUMN calendario.observacoes_nota_revisoes.atualizado_por IS 'Identificador do usuario que modificou a revisao.';

-- grupo_paroquial
ALTER TABLE calendario.grupo_paroquial ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.grupo_paroquial ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.grupo_paroquial ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.grupo_paroquial ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.grupo_paroquial.criado_em_utc IS 'Timestamp UTC de criacao do grupo paroquial.';
COMMENT ON COLUMN calendario.grupo_paroquial.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do grupo paroquial.';
COMMENT ON COLUMN calendario.grupo_paroquial.criado_por IS 'Identificador do usuario que cadastrou o grupo.';
COMMENT ON COLUMN calendario.grupo_paroquial.atualizado_por IS 'Identificador do usuario que modificou o grupo.';

-- unidade_territorial
ALTER TABLE calendario.unidade_territorial ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.unidade_territorial ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.unidade_territorial ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.unidade_territorial ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.unidade_territorial.criado_em_utc IS 'Timestamp UTC de criacao da unidade territorial.';
COMMENT ON COLUMN calendario.unidade_territorial.atualizado_em_utc IS 'Timestamp UTC de atualizacao da unidade territorial.';
COMMENT ON COLUMN calendario.unidade_territorial.criado_por IS 'Identificador do usuario que cadastrou a unidade.';
COMMENT ON COLUMN calendario.unidade_territorial.atualizado_por IS 'Identificador do usuario que modificou a unidade.';

-- pastorais_movimentos_oficiais
ALTER TABLE calendario.pastorais_movimentos_oficiais ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.pastorais_movimentos_oficiais ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.pastorais_movimentos_oficiais ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.pastorais_movimentos_oficiais ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.criado_em_utc IS 'Timestamp UTC de criacao do catalogo oficial.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.atualizado_em_utc IS 'Timestamp UTC de atualizacao do catalogo.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.criado_por IS 'Identificador do usuario que registrou o movimento.';
COMMENT ON COLUMN calendario.pastorais_movimentos_oficiais.atualizado_por IS 'Identificador do usuario que modificou o movimento.';

-- projetos_eventos
ALTER TABLE calendario.projetos_eventos ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.projetos_eventos ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.projetos_eventos ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.projetos_eventos ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.projetos_eventos.criado_em_utc IS 'Timestamp UTC de criacao do projeto paroquial.';
COMMENT ON COLUMN calendario.projetos_eventos.atualizado_em_utc IS 'Timestamp UTC de atualizacao do projeto paroquial.';
COMMENT ON COLUMN calendario.projetos_eventos.criado_por IS 'Identificador do usuario que criou o projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.atualizado_por IS 'Identificador do usuario que atualizou o projeto.';

-- anos_liturgicos
ALTER TABLE calendario.anos_liturgicos ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.anos_liturgicos ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.anos_liturgicos ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.anos_liturgicos.criado_em_utc IS 'Timestamp UTC de criacao do ano liturgico.';
COMMENT ON COLUMN calendario.anos_liturgicos.criado_por IS 'Identificador do usuario que cadastrou o ano liturgico.';
COMMENT ON COLUMN calendario.anos_liturgicos.atualizado_por IS 'Identificador do usuario que modificou o ano liturgico.';

-- tb_ciclo_liturgico
ALTER TABLE calendario.tb_ciclo_liturgico ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.tb_ciclo_liturgico ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.tb_ciclo_liturgico ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.tb_ciclo_liturgico ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.tb_ciclo_liturgico.criado_em_utc IS 'Timestamp UTC de criacao do ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.atualizado_em_utc IS 'Timestamp UTC de modificacao do ciclo liturgico.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.criado_por IS 'Identificador do usuario que cadastrou o ciclo.';
COMMENT ON COLUMN calendario.tb_ciclo_liturgico.atualizado_por IS 'Identificador do usuario que atualizou o ciclo.';

-- tb_solenidade_liturgica
ALTER TABLE calendario.tb_solenidade_liturgica ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.tb_solenidade_liturgica ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.tb_solenidade_liturgica ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.tb_solenidade_liturgica ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.tb_solenidade_liturgica.criado_em_utc IS 'Timestamp UTC de criacao da solenidade.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.atualizado_em_utc IS 'Timestamp UTC de modificacao da solenidade.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.criado_por IS 'Identificador do usuario que registrou a solenidade.';
COMMENT ON COLUMN calendario.tb_solenidade_liturgica.atualizado_por IS 'Identificador do usuario que atualizou a solenidade.';

-- paroquia_configuracao
ALTER TABLE calendario.paroquia_configuracao ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.paroquia_configuracao ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.paroquia_configuracao ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.paroquia_configuracao ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.paroquia_configuracao.criado_em_utc IS 'Timestamp UTC da configuracao inicial da paroquia.';
COMMENT ON COLUMN calendario.paroquia_configuracao.atualizado_em_utc IS 'Timestamp UTC da ultima alteracao das configuracoes.';
COMMENT ON COLUMN calendario.paroquia_configuracao.criado_por IS 'Identificador do usuario que configurou.';
COMMENT ON COLUMN calendario.paroquia_configuracao.atualizado_por IS 'Identificador do usuario que modificou.';

-- patrimonio_numeracao_anual
ALTER TABLE calendario.patrimonio_numeracao_anual ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.patrimonio_numeracao_anual ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.patrimonio_numeracao_anual ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.patrimonio_numeracao_anual ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.criado_em_utc IS 'Timestamp UTC de inicializacao da sequencia anual de patrimonio.';
COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.atualizado_em_utc IS 'Timestamp UTC da ultima alteracao da numeracao.';
COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.criado_por IS 'Identificador do sistema.';
COMMENT ON COLUMN calendario.patrimonio_numeracao_anual.atualizado_por IS 'Identificador do sistema.';

-- auditoria_operacoes
ALTER TABLE calendario.auditoria_operacoes ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.auditoria_operacoes ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.auditoria_operacoes ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.auditoria_operacoes ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.auditoria_operacoes.criado_em_utc IS 'Timestamp UTC de criacao do registro de auditoria operacional.';
COMMENT ON COLUMN calendario.auditoria_operacoes.atualizado_em_utc IS 'Timestamp UTC de atualizacao do registro de auditoria operacional.';
COMMENT ON COLUMN calendario.auditoria_operacoes.criado_por IS 'Identificador do autor do registro.';
COMMENT ON COLUMN calendario.auditoria_operacoes.atualizado_por IS 'Identificador do modificador do registro.';

-- job_locks
ALTER TABLE calendario.job_locks ADD COLUMN IF NOT EXISTS criado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.job_locks ADD COLUMN IF NOT EXISTS atualizado_em_utc TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW();
ALTER TABLE calendario.job_locks ADD COLUMN IF NOT EXISTS criado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';
ALTER TABLE calendario.job_locks ADD COLUMN IF NOT EXISTS atualizado_por VARCHAR(128) NOT NULL DEFAULT 'sistema';

COMMENT ON COLUMN calendario.job_locks.criado_em_utc IS 'Timestamp UTC de criacao do lock distribuido.';
COMMENT ON COLUMN calendario.job_locks.atualizado_em_utc IS 'Timestamp UTC da ultima atualizacao do lock distribuido.';
COMMENT ON COLUMN calendario.job_locks.criado_por IS 'Identificador da instancia/sistema que criou o lock.';
COMMENT ON COLUMN calendario.job_locks.atualizado_por IS 'Identificador da instancia/sistema que atualizou o lock.';

