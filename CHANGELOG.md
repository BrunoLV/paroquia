# Changelog - Projeto Paróquia

Todas as alterações notáveis neste projeto serão documentadas neste arquivo.

O formato é baseado no [Keep a Changelog](https://keepachangelog.com/pt-BR/1.0.0/), e este projeto adere ao [Semantic Versioning](https://semver.org/lang/pt-BR/).

---

## [Unreleased]

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

### Modificado
- **Docker:** compose sem credenciais padrão (`.env` obrigatório, `${VAR:?}`), portas de infraestrutura em `127.0.0.1`, `db-seeder` e `ui-dev` apenas no perfil `dev`; `Dockerfile.ui` agora gera build estático servido por nginx sem privilégios (alvo `dev` separado); Loki com retenção de 7 dias; Prometheus autenticado por token; comentário "Spring Boot 3" corrigido. `DOCKER.md` e `docs/guide/docker.md` atualizados.

---

## [0.12.1] - 2026-10-25
