-- V086: Documentação DDL (COMMENT ON) das tabelas e colunas mais centrais do domínio,
-- conforme regra "Comentários de Domínio Obrigatórios" (sql.md). Apenas COMMENT ON, DDL
-- não destrutivo e seguro para reexecução (idempotente por natureza: sobrescreve o
-- comentário anterior sem alterar dados ou estrutura). calendario.reservas já documentada
-- em V082 — não repetida aqui. Colunas comentadas são restritas às que existem de fato no
-- schema atual (após todos os ALTER/RENAME/DROP aplicados até V085).

-- ===================== eventos =====================
COMMENT ON TABLE calendario.eventos IS 'Compromissos da agenda paroquial (missas, reuniões, solenidades) sujeitos a fluxo de aprovação e conflito de local.';
COMMENT ON COLUMN calendario.eventos.id IS 'Identificador primário do evento (UUID).';
COMMENT ON COLUMN calendario.eventos.titulo IS 'Título exibido na agenda.';
COMMENT ON COLUMN calendario.eventos.descricao IS 'Descrição livre do evento.';
COMMENT ON COLUMN calendario.eventos.grupo_paroquial_responsavel_id IS 'Grupo paroquial ou unidade territorial responsável pelo evento (referência polimórfica resolvida em nível de aplicação, ver V066).';
COMMENT ON COLUMN calendario.eventos.inicio_utc IS 'Instante de início do evento em UTC.';
COMMENT ON COLUMN calendario.eventos.fim_utc IS 'Instante de término do evento em UTC.';
COMMENT ON COLUMN calendario.eventos.status IS 'Estado do fluxo de aprovação/execução do evento.';
COMMENT ON COLUMN calendario.eventos.cancelado_motivo IS 'Justificativa obrigatória quando o evento é cancelado.';
COMMENT ON COLUMN calendario.eventos.categoria IS 'Categoria litúrgica/administrativa do evento.';
COMMENT ON COLUMN calendario.eventos.local_id IS 'Local físico reservado para o evento, quando aplicável.';
COMMENT ON COLUMN calendario.eventos.recorrencia_id IS 'Identificador de série para eventos recorrentes.';
COMMENT ON COLUMN calendario.eventos.nivel_prioridade IS 'Prioridade de agenda usada para resolução de conflitos entre eventos concorrentes.';
COMMENT ON COLUMN calendario.eventos.version IS 'Controle de versão para concorrência otimista (JPA).';

-- ===================== fieis =====================
COMMENT ON TABLE calendario.fieis IS 'Cadastro de fiéis (leigos) da paróquia: dados civis, contato e vínculos sacramentais/territoriais, sob regime LGPD.';
COMMENT ON COLUMN calendario.fieis.id IS 'Identificador primário do fiel (UUID).';
COMMENT ON COLUMN calendario.fieis.nome IS 'Nome civil completo do fiel.';
COMMENT ON COLUMN calendario.fieis.cpf IS 'CPF do fiel, armazenado de forma reversível para exibição autorizada.';
COMMENT ON COLUMN calendario.fieis.cpf_hash IS 'Hash irreversível do CPF, usado como chave de unicidade e busca sem expor o dado sensível.';
COMMENT ON COLUMN calendario.fieis.data_nascimento IS 'Data de nascimento do fiel.';
COMMENT ON COLUMN calendario.fieis.email IS 'E-mail de contato do fiel.';
COMMENT ON COLUMN calendario.fieis.telefone IS 'Telefone de contato do fiel.';
COMMENT ON COLUMN calendario.fieis.ativo IS 'Indica se o cadastro está ativo (falso em caso de desativação/ciclo de vida LGPD).';
COMMENT ON COLUMN calendario.fieis.vocacao IS 'Vocação/estado de vida eclesial do fiel (leigo, religioso etc.).';
COMMENT ON COLUMN calendario.fieis.unidade_territorial_id IS 'Unidade territorial (matriz/comunidade) à qual o fiel está vinculado.';
COMMENT ON COLUMN calendario.fieis.is_dizimista IS 'Indica se o fiel é dizimista cadastrado.';
COMMENT ON COLUMN calendario.fieis.criado_em_utc IS 'Data e hora de criação do cadastro.';
COMMENT ON COLUMN calendario.fieis.atualizado_em_utc IS 'Data e hora da última atualização do cadastro.';
COMMENT ON COLUMN calendario.fieis.criado_por IS 'Usuário/processo que criou o registro.';
COMMENT ON COLUMN calendario.fieis.atualizado_por IS 'Usuário/processo que fez a última atualização.';
COMMENT ON COLUMN calendario.fieis.version IS 'Controle de versão para concorrência otimista (JPA).';

-- ===================== usuarios =====================
COMMENT ON TABLE calendario.usuarios IS 'Contas de acesso ao sistema (login), distintas do cadastro pastoral de fiel/clérigo a que podem estar vinculadas.';
COMMENT ON COLUMN calendario.usuarios.id IS 'Identificador primário da conta de usuário (UUID).';
COMMENT ON COLUMN calendario.usuarios.username IS 'Login único usado na autenticação.';
COMMENT ON COLUMN calendario.usuarios.password_hash IS 'Hash da senha (nunca texto plano).';
COMMENT ON COLUMN calendario.usuarios.enabled IS 'Indica se a conta está habilitada para login.';
COMMENT ON COLUMN calendario.usuarios.roles IS 'Papéis/autoridades atribuídos à conta, usados no controle de acesso.';
COMMENT ON COLUMN calendario.usuarios.fiel_id IS 'Fiel associado a esta conta de acesso, quando existente.';
COMMENT ON COLUMN calendario.usuarios.clerigo_id IS 'Clérigo associado a esta conta de acesso, quando existente.';
COMMENT ON COLUMN calendario.usuarios.igreja_id IS 'Unidade territorial (igreja) de referência da conta.';
COMMENT ON COLUMN calendario.usuarios.criado_em_utc IS 'Data e hora de criação da conta.';
COMMENT ON COLUMN calendario.usuarios.atualizado_em_utc IS 'Data e hora da última atualização da conta.';
COMMENT ON COLUMN calendario.usuarios.version IS 'Controle de versão para concorrência otimista (JPA).';

-- ===================== aprovacoes =====================
COMMENT ON TABLE calendario.aprovacoes IS 'Solicitações de aprovação vinculadas a eventos, tramitando por papel aprovador até deferimento/indeferimento.';
COMMENT ON COLUMN calendario.aprovacoes.id IS 'Identificador primário da solicitação de aprovação (UUID).';
COMMENT ON COLUMN calendario.aprovacoes.evento_id IS 'Evento ao qual esta solicitação de aprovação se refere.';
COMMENT ON COLUMN calendario.aprovacoes.tipo_solicitacao IS 'Natureza da solicitação (ex.: criação, alteração, cancelamento de evento).';
COMMENT ON COLUMN calendario.aprovacoes.aprovador_papel IS 'Papel eclesiástico/administrativo com alçada para decidir a solicitação.';
COMMENT ON COLUMN calendario.aprovacoes.status IS 'Estado da solicitação (pendente, aprovada, rejeitada etc.).';
COMMENT ON COLUMN calendario.aprovacoes.solicitante_tipo_grupo_paroquial IS 'Tipo de grupo paroquial do solicitante, usado para determinar a alçada de aprovação.';
COMMENT ON COLUMN calendario.aprovacoes.mensagem_erro_execucao IS 'Mensagem de erro registrada quando a execução automática da ação aprovada falha.';
COMMENT ON COLUMN calendario.aprovacoes.criado_em_utc IS 'Data e hora de criação da solicitação.';
COMMENT ON COLUMN calendario.aprovacoes.decidido_em_utc IS 'Data e hora em que a solicitação foi decidida (aprovada ou rejeitada).';
COMMENT ON COLUMN calendario.aprovacoes.version IS 'Controle de versão para concorrência otimista (JPA).';

-- ===================== ativos_patrimoniais =====================
COMMENT ON TABLE calendario.ativos_patrimoniais IS 'Bens patrimoniais móveis da paróquia (equipamentos, veículos etc.) sujeitos a tombamento, alocação e reserva.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.id IS 'Identificador primário do ativo patrimonial (UUID).';
COMMENT ON COLUMN calendario.ativos_patrimoniais.nome IS 'Nome/descrição curta do ativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.codigo_tombamento IS 'Código único de tombamento patrimonial do ativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.categoria IS 'Tipo/categoria do ativo (exibido como "Tipo" na UI).';
COMMENT ON COLUMN calendario.ativos_patrimoniais.status IS 'Estado atual do ativo (ex.: disponível, em manutenção, baixado).';
COMMENT ON COLUMN calendario.ativos_patrimoniais.is_reservavel IS 'Indica se o ativo pode ser reservado via calendario.reservas.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.grupo_paroquial_id IS 'Grupo paroquial responsável/proprietário do ativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.data_aquisicao IS 'Data de aquisição do ativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.valor_aquisicao IS 'Valor de aquisição do ativo, em reais.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.descricao IS 'Descrição detalhada do ativo.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.deleted_at IS 'Data de exclusão lógica (soft delete).';
COMMENT ON COLUMN calendario.ativos_patrimoniais.criado_em_utc IS 'Data e hora de criação do registro.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.updated_at IS 'Data e hora da última atualização do registro.';
COMMENT ON COLUMN calendario.ativos_patrimoniais.version IS 'Controle de versão para concorrência otimista (JPA).';

-- ===================== projetos_eventos =====================
COMMENT ON TABLE calendario.projetos_eventos IS 'Projetos que agrupam múltiplos eventos sob um objetivo pastoral comum, com fluxo de gestão e aprovação próprios.';
COMMENT ON COLUMN calendario.projetos_eventos.id IS 'Identificador primário do projeto (UUID).';
COMMENT ON COLUMN calendario.projetos_eventos.nome IS 'Nome do projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.descricao IS 'Descrição do objetivo/escopo do projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.status IS 'Estado do projeto (ex.: ativo, encerrado).';
COMMENT ON COLUMN calendario.projetos_eventos.grupo_paroquial_responsavel_id IS 'Grupo paroquial responsável pelo projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.inicio_utc IS 'Data/hora de início planejado do projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.fim_utc IS 'Data/hora de término planejado do projeto.';
COMMENT ON COLUMN calendario.projetos_eventos.version IS 'Controle de versão para concorrência otimista (JPA).';

-- ===================== grupo_paroquial =====================
COMMENT ON TABLE calendario.grupo_paroquial IS 'Pastorais, movimentos, conselhos e ministérios da paróquia (grupos de atuação), distintos das unidades territoriais (matriz/comunidades) — ver V064.';
COMMENT ON COLUMN calendario.grupo_paroquial.id IS 'Identificador primário do grupo paroquial (UUID).';
COMMENT ON COLUMN calendario.grupo_paroquial.nome IS 'Nome do grupo paroquial.';
COMMENT ON COLUMN calendario.grupo_paroquial.tipo IS 'Tipo do grupo (PASTORAL, MOVIMENTO, CONSELHO, MINISTERIO).';
COMMENT ON COLUMN calendario.grupo_paroquial.contato IS 'Informação de contato do grupo.';
COMMENT ON COLUMN calendario.grupo_paroquial.ativo IS 'Indica se o grupo está ativo.';
COMMENT ON COLUMN calendario.grupo_paroquial.parent_id IS 'Grupo paroquial ou unidade territorial superior na hierarquia (referência polimórfica validada em nível de aplicação).';
COMMENT ON COLUMN calendario.grupo_paroquial.version IS 'Controle de versão para concorrência otimista (JPA).';

-- ===================== unidade_territorial =====================
COMMENT ON TABLE calendario.unidade_territorial IS 'Divisões territoriais da paróquia (matriz, comunidades) e o clero, distintas dos grupos paroquiais de atuação pastoral — ver V063.';
COMMENT ON COLUMN calendario.unidade_territorial.id IS 'Identificador primário da unidade territorial (UUID).';
COMMENT ON COLUMN calendario.unidade_territorial.nome IS 'Nome da unidade territorial.';
COMMENT ON COLUMN calendario.unidade_territorial.tipo IS 'Tipo da unidade territorial (ex.: MATRIZ, COMUNIDADE, CLERO).';
COMMENT ON COLUMN calendario.unidade_territorial.contato IS 'Informação de contato da unidade.';
COMMENT ON COLUMN calendario.unidade_territorial.ativo IS 'Indica se a unidade está ativa.';
COMMENT ON COLUMN calendario.unidade_territorial.parent_id IS 'Unidade territorial superior na hierarquia, quando aplicável.';
COMMENT ON COLUMN calendario.unidade_territorial.version IS 'Controle de versão para concorrência otimista (JPA).';
