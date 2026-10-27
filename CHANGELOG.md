# Changelog - Projeto Paróquia

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/), e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [Unreleased]

## [0.13.0] - 2026-10-26

### Adicionado
- `PATCH /api/v1/reservas/{id}/finalizar` e ação "Finalizar Uso" na tela de reservas (RN-RES-06).
- **Fronteiras arquiteturais protegidas por ArchUnit:** regras de encapsulamento entre contextos (um contexto não acessa `application`/`infrastructure` de outro; `support` é o núcleo compartilhado), de dependência mútua entre contextos e de proibição de chamar setters de auditoria fora das entidades. O legado é congelado com `FreezingArchRule` em `app/src/test/resources/archunit_store`; qualquer violação nova falha o build.
- **Qualidade do Frontend:** ESLint (flat config com `typescript-eslint`, `react-hooks`, `jsx-a11y`) e Prettier, scripts `lint`, `format:check` e `typecheck:tests`; regra `no-restricted-imports` impedindo React/MUI/TanStack/Zod/Axios em `contextos/*/domain` e `application` e Axios fora de `infrastructure`. Regras legadas ruidosas (`no-explicit-any`, `no-unused-vars`, `no-autofocus`, `no-useless-catch`) ficam como `warn` documentado em `eslint.config.js`.
- **Testes:** cobertura mínima para `membro`, `projeto`, `patrimonio` e `importacao`; piso de cobertura no Vitest (statements/lines 70%, branches 66%, functions 45%).
- **CI:** lint e Prettier do frontend, type-check informativo dos testes, `npm audit` e dependency review informativos, job de build das imagens Docker (sem push) e `.github/dependabot.yml` (gradle, npm, github-actions, docker, docker-compose).
- **Gradle:** `distributionSha256Sum` no wrapper; `spring-boot-starter-quartz` no `libs.versions.toml`.

### Segurança / LGPD
- **Segredos obrigatórios:** `APP_CRYPTO_KEY` e `APP_CONSENT_SECRET` (>= 32 caracteres) deixam de ter valor padrão; a API aborta o boot sem eles. Perfil `dev` (`application-dev.yml`, ativado por `./manage.sh dev`) traz valores explícitos de desenvolvimento.
- **Login e reset de senha:** limite de tentativas por IP + usuário (5 falhas/15 min no login; 3 pedidos/15 min no reset) com resposta 429 e `Retry-After`.
- **Superfície pública reduzida:** `/actuator/metrics`, OpenAPI e Swagger exigem ADMIN (Swagger/OpenAPI desligados fora do perfil dev); `/actuator/prometheus` aceita ADMIN ou token Bearer dedicado (`APP_METRICS_SCRAPE_TOKEN`); `POST /api/v1/observabilidade/logs` exige autenticação e neutraliza caracteres de controle (log forging).
- **Sessão e cabeçalhos:** cookie `SameSite=Lax`, `HttpOnly` e `Secure` (configurável por `SESSION_COOKIE_SECURE`), CSP, HSTS e Referrer-Policy.
- **Política única de senha** (`@SenhaValida`: mín. 8 caracteres, máx. 72 bytes) em criação, alteração e redefinição; UI alinhada.
- **Correlation id:** `X-Correlation-Id` recebido só é ecoado se for token seguro (`[A-Za-z0-9._-]{1,64}`); caso contrário gera UUID.
- **Rate limit atrás de proxy:** `app.security.rate-limit.trusted-proxies` (`APP_TRUSTED_PROXIES`); default usa só o IP da conexão.
- **Flyway:** `baseline-on-migrate` desligado fora do perfil dev.
- **Cifra e blind index:** `EncryptedStringConverter`/`CryptoUtils` passam a AES-256/GCM com prefixo de versão de chave (`v1:`), leem o formato CBC legado e lançam erro (em vez de devolver o ciphertext) quando a decifragem falha. O `cpf_hash` troca SHA-256 puro por HMAC-SHA256 com pepper (`app.security.cpf-pepper`, **obrigatória e sem valor padrão**); clérigos ganham o mesmo blind index e a busca pública por CPF deixa de decifrar todos os clérigos.
- **PII do DPO e da trilha de consentimento cifrada** (`solicitacoes_dpo`, `historico_consentimentos`); a consulta pública do protocolo devolve nome/e-mail/CPF mascarados. Corrigida a coluna `fieis.responsavel_menor_cpf`, que era cifrada em `VARCHAR(14)`.
- **Portal de consentimento:** máximo de 3 pedidos de código por 15 minutos, resposta anti-enumeração idêntica, contador de tentativas efetivamente persistido (antes revertia junto com a exceção), token de consentimento de uso único, revogação força autorizações a `false`, CPF do responsável do menor validado e IP de origem sem confiar em `X-Forwarded-For`.
- **E-mail real** do código OTP e do comprovante via `spring-boot-starter-mail` (`app.mail.enabled`, `app.mail.remetente`, `spring.mail.*`); por padrão nada é enviado.
- **Anonimização completa** do fiel (CPF hash, nascimento, sexo, dependentes, relações, trilha, conta) e nova anonimização do clérigo com job diário; trilha de consentimento *append-only* por gatilho.
- **DSAR e incidentes:** máquinas de estado, protocolo `DPO-AAAAMMDD-XXXXXXXXXXXX`, prazos de 15 dias e de 3 dias úteis sinalizados; `IncidenteSeguranca` de domínio em uso.
- **Pedidos de OTP atômicos** (advisory lock por titular), **recifragem CBC→GCM** na subida (fiéis, dependentes, clérigos e DPO), **feriados nacionais** no prazo de 3 dias úteis do incidente e portal de consentimento sem revelar o e-mail mascarado (anti-enumeração).
- **Migrações:** V040 (expansão), V041 (migração Java de dados: cifra e rehash, idempotente) e V042 (gatilho append-only).
- **Reserva e Local:** retirada e devolução de ativo restritas a solicitante, coordenador e administração; `ReservaSecurityAdapter` nega acesso (fail-closed) sem usuário autenticado em vez de assumir ADMIN; buffer legado acima de 240 min é limitado na leitura.
- `AuthContext` não persiste mais o `user` (PII) em `localStorage`; o cache legado é purgado no carregamento.

### Modificado
- **Docker:** compose sem credenciais padrão (`.env` obrigatório, `${VAR:?}`), portas de infraestrutura em `127.0.0.1`, `db-seeder` e `ui-dev` apenas no perfil `dev`; `Dockerfile.ui` agora gera build estático servido por nginx sem privilégios (alvo `dev` separado); Loki com retenção de 7 dias; Prometheus autenticado por token; comentário "Spring Boot 3" corrigido. `DOCKER.md` e `docs/guide/docker.md` atualizados.
- **TypeScript:** `target` ES2022; `tsconfig.test.json` checa os arquivos de teste; shim de tipos do Vitest substituído por tipos reais.
- **SpotBugs bloqueante:** `ignoreFailures=false`, com baseline por classe/padrão em `config/spotbugs/spotbugs-filter.xml` (débitos reais sinalizados: `GerenciarRelacionamentoFamiliarUseCase` e `CorrelationIdFilter`).
- **Node 22** alinhado entre CI, READMEs, `engines` e `Dockerfile.ui`; `AGENTS.md` raiz ajustado: Convention Plugins em `build-logic` só ao surgir o segundo módulo Gradle.
- `Projetos.tsx` passa a usar adapter e hooks do contexto `projeto`; detecção de erro HTTP em `errorUtils`/`useEventoCrud` sem importar Axios.

### Corrigido
- **Autorização e escalonamento de privilégios (backend):**
  - `iam`: redefinição administrativa de senha restrita a `ADMIN` (qualquer conta) e `PAROCO` (apenas contas sem papel global); coordenador não redefine mais senhas.
  - `organizacao`: remoção de vínculo valida que o vínculo pertence ao fiel da rota e que o ator coordena a organização dele (IDOR). Vínculo administrativo: coordenação só pelo Pároco/Bispo e com idoneidade; membros de conselho só por Pároco/Admin.
  - `clero`: conferir ou mudar a função de um clérigo exige Pároco ou Bispo; reenviar a mesma função não fecha/reabre o encargo; um único Pároco/Administrador Paroquial ativo.
  - `membro`: `/membros` não atribui mais `COORDENADOR` nem papéis de conselho (nem reativa vínculos de liderança); promoção a coordenador só pelo Pároco/Bispo e com limite de um coordenador ativo por organização.
  - `aprovacao`: decisão só aceita `APROVADA`/`REPROVADA`; reprovação exige parecer; payload ilegível ou verificação de conflito impossível bloqueia a aprovação (falha fechada); Bispo Diocesano reconhecido como aprovador; listagens restritas a quem decide ou ao solicitante, sem detalhe técnico de falha para os demais.
  - `calendario`: `EventoActorContextResolver` nega acesso a principal sem papel reconhecido em vez de assumi-lo coordenador.
  - `membro`/`organizacao`: vice-coordenação (secretário de grupo) passa a exigir idoneidade (RN-MEM-04) na promoção e no vínculo administrativo e deixa de ser atribuível em `/membros`; homologar, vetar e encerrar mandato de coordenação é só do Pároco (RN-ORG-05), o Bispo apenas propõe.
  - `fiel`: auditoria de relacionamento familiar não falha mais quando a origem não tem unidade territorial (NPE apontado pelo SpotBugs).
  - `fiel`: consulta por papel (completa, contato do grupo ou somente id e nome) e papéis globais não são mais limitados pela organização do vínculo.
- **Reserva e Local (consistência):**
  - `POST /api/v1/reservas` não aceita mais `eventoId` nem `grupoRecorrenciaId`: qualquer solicitante se autoconfirmava informando um evento. Esses vínculos só nascem pela integração com eventos e pela série recorrente.
  - Dupla reserva: a criação passa a bloquear a linha do local/ativo antes de consultar conflitos (teste de concorrência com Testcontainers). A janela de busca deriva do buffer máximo (240 min, agora imposto pelo `IntervaloReserva`) em vez de ±4h fixas.
  - `ReservaCriacaoService` único para pedido avulso, série recorrente e eventos: valida existência/atividade/`reservavel` do item, conflito, janela de manutenção do ativo e publica `RESERVADO`. A integração com eventos deixou de gerar `UUID` aleatório para solicitante, operador e pastoral (falha de imediato) e a cascata não tenta mais cancelar reservas retiradas ou finalizadas.
  - Reserva `RETIRADO` não pode ser cancelada; transições de estado ilegais respondem 400 (antes 500).
  - Expiração de pendentes: uma transação por reserva, sem `catch (Exception ignored)`, e pedido feito com menos de 48 h de antecedência só expira no início.
  - Corrida manutenção × reserva: `EnviarAtivoParaManutencao` bloqueia a linha do ativo, a mesma tomada na criação de reservas.
  - Local: inativação/exclusão consideram reservas em aberto; removida a remoção física (`LocalRepository.delete`).
- **Importação em lote respeita as regras do domínio de destino:**
  - Eventos importados passam pelo caso de uso de criação do calendário (autorização, ano paroquial fechado, conflito e esteira de aprovação); o status deixa de vir da planilha.
  - Fim dos valores assumidos em silêncio (tipo de organização, organização responsável, organização-mãe, catálogo oficial, datas, categoria): a linha é rejeitada com mensagem clara.
  - O dry-run usa o mesmo parse e as mesmas validações da confirmação.
  - A confirmação grava cada linha em transação própria e devolve o total importado, o total enviado para aprovação e os erros por linha (corrige `UnexpectedRollbackException` e perda do lote); a UI exibe as linhas rejeitadas.
  - Importação de membros delega a `AdicionarVinculoUseCase` e nunca atribui coordenação, vice-coordenação nem papéis de conselho.
  - Entrada endurecida: corpo validado, limites de 2 MB e 1.000 linhas, sem ator padrão `system_admin`, sem PII em log.
  - Modelos de planilha corrigidos (papéis válidos; colunas de setor, subtipo e justificativa).
  - UI de importação deixa de oferecer o tipo "Usuários", inexistente no backend.
  - Chave de idempotência de evento excluído é descartada, permitindo reimportá-lo (antes o replay falhava).
  - `OrganizacaoService` audita com o principal autenticado (ou `sistema` sem autenticação) em vez do ator fixo `admin`.
  - **Contrato:** `POST /api/v1/importacao/confirmar` passa a receber `{ "itens": [{ "linha", "dados" }] }` e a responder `{ registrosImportados, enviadosParaAprovacao, erros[] }`.
- **Fuso horário único de negócio:** novo `RelogioNegocio` (relógio + `America/Sao_Paulo`, configurável em `app.timezone.output`) usado no ano paroquial de eventos e projetos, exportação mensal/anual, recorrência (dia civil e horário local preservados), janelas e métricas do painel, aniversariantes (29/02 comemora em 28/02 em ano não bissexto) e crons da recorrência anual e da ativação de projetos.
- **Recorrência anual:** as ocorrências materializadas passam pela detecção de conflito (as conflitantes não são geradas), herdam ativos e envolvidos e criam a reserva de local; a criação de uma série gera só a regra afetada e não há mais consulta por data.
- Rotina anual de recorrência: cada ocorrência em transação própria (falha isolada e registrada); fuso da paróquia também em `PeriodoProjetoVincularPolicy`, `AnoParoquialFechadoPolicy`, `MotorLiturgico` e nos serviços de ano paroquial (rollover com cron no fuso); cache de aniversariantes com o dia de negócio na chave.
- `CriarEventoUseCase` com construtor único e dependências obrigatórias (sem fallback que silenciava a detecção de conflito).
- **Painel:** eventos cancelados fora das métricas; "pendentes de aprovação" do projeto conta aprovações pendentes; escopo organizacional aplicado a fiéis e aprovações (resumo e pendências de homologação); engajamento exige fiel ativo; ocupação de ambientes derivada de reservas de local por organização responsável, com sobreposições unidas; consultas agregadas no lugar de `findAll()` e N+1 de projetos.
- **Projeto:** redefinir o período revalida os eventos vinculados (RN-PROJ-03); a transferência de responsável exige alçada sobre a organização de destino, existente e ativa; a ativação automática usa `Projeto.iniciar()`.
- **Sacramento:** a certidão anexa as ressalvas atestadas ao texto.
- **ArchUnit deixava de analisar o código (regras vacuosas):** o ArchUnit 1.4.0 não lê bytecode do Java 25 e importava 0 classes, então todas as regras passavam sem verificar nada (mascarado por `archRule.failOnEmptyShould=false`). Atualizado para ArchUnit 1.4.2 (`libs.versions.toml`) e `failOnEmptyShould=true`; regra sem classes analisadas agora falha.
- **Pureza do domínio:** a normalização de enums (`EnumRequestNormalizer`) passa para `support.domain` (função pura); `@JsonCreator`/`@JsonValue` saem dos 13 enums de domínio e o vínculo JSON fica em mix-ins `@JacksonMixin` por contexto na camada `api` (desserialização das requisições inalterada); a policy de gerenciamento de usuários do IAM lança `GerenciamentoUsuarioNegadoException` em vez da `AccessDeniedException` do Spring, mapeada para 403/`ACCESS_DENIED` no `SecurityExceptionHandler` (corpo idêntico).
- **Auditoria manual removida:** casos de uso, mappers e serviço de idempotência não preenchem mais `criadoEmUtc`/`atualizadoEmUtc`/`criadoPor`; os setters de `BaseAuditableEntity` são `protected` e os aliases `setCreatedAt`/`setUpdatedAt` foram removidos. Os construtores de reidratação das entidades mantêm o instante já persistido para o `merge`.
- **`@Order` dos advices:** segurança 1, contextos 2, validação 3, global `LOWEST_PRECEDENCE` explícito (substitui o `@Order()` vazio).
- `FielViewModal` e `ClerigoViewModal`: `useQuery` chamado após retorno antecipado (violação de rules-of-hooks).

---

## [0.12.1] - 2026-10-25
