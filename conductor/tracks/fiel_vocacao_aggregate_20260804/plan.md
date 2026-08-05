# Plan: Agregado `Fiel` e Enum `Vocacao`

## Phase 0: Setup
- [x] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b feat/fiel-vocacao-aggregate-<data>`) — branch `feat/fiel-vocacao-aggregate-20260804`
- [x] Task: Confirmar próximo número de migração Flyway livre (`V056` em diante) e reservar o nome do arquivo — próximo livre: `V056`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `00cd10f`

## Phase 1: Enum `Vocacao`
- [x] Task: Escrever testes (Red) para `Vocacao.fromStoredValue`/`fromJson` cobrindo os 4 valores
      (`LEIGO, SACERDOTE, DIACONO, RELIGIOSO`), normalização de caixa/separador e valor inválido/nulo `4770b04`
- [x] Task: Implementar o enum `Vocacao` em `fiel/domain/model/Vocacao.java`, seguindo o padrão de
      `TipoOrganizacao`/`PapelOrganizacional` (Green) `4770b04`
- [x] Task: Refatorar se necessário — não necessário, implementação já segue o padrão estabelecido
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `980ad8d`

## Phase 2: Entidade `Fiel` e tabela `fieis`
- [x] Task: Escrever teste de integração (Red) que persiste um `Fiel` válido e falha ao tentar persistir
      dois `Fiel` com o mesmo `cpf` `a3ec410`
- [x] Task: Criar migração Flyway `V056__create_fieis_table.sql` (schema `calendario`, todas as colunas de
      FR1 obrigatórias, constraint de unicidade) `a3ec410`
- [x] Task: Implementar `Fiel` (domain/model), `FielEntity` (infrastructure/persistence/entity, estendendo
      `BaseVersionedEntity`, aplicando `EncryptedStringConverter`/`EncryptedLocalDateConverter` em
      `nome/cpf/telefone/dataNascimento/endereco`) e `FielJpaRepository` (Green) `a3ec410`
- [x] Task: Refatorar se necessário — corrigido hint de lint (catch específico de `NoSuchAlgorithmException`
      em vez de `Exception` genérico) `a3ec410`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `0f743b1`

  **Desvio descoberto durante a implementação:** a constraint de unicidade não pôde ser aplicada
  diretamente sobre `cpf`, pois `EncryptedStringConverter` usa IV aleatório por chamada (o mesmo CPF em
  claro produz ciphertext diferente a cada gravação). Adicionada uma coluna `cpf_hash` (SHA-256
  determinístico, não-reversível, mantida em sincronia por `FielEntity.setCpf()`), com a constraint
  `UNIQUE` aplicada nela em vez de em `cpf`. Confirmado com o usuário antes de implementar — ver git note
  do commit `a3ec410`.

## Phase 3: `UsuarioEntity.fielId` + auditoria (Fiel e Usuario)
- [x] Task: Escrever testes (Red) confirmando que `UsuarioEntity` aceita `fielId` nullable e possui os
      novos campos de auditoria (`criadoEmUtc`, `atualizadoEmUtc`, `criadoPor`, `atualizadoPor`) `950b675`
- [x] Task: Criar migração Flyway adicionando `fiel_id` (nullable), `criado_em_utc`, `atualizado_em_utc`,
      `criado_por`, `atualizado_por` em `usuarios` (V057) `950b675`
- [x] Task: Implementar os campos em `UsuarioEntity` (Green) `950b675`
- [x] Task: Escrever testes (Red) para `CreateUsuarioUseCase`, `RegistrarUsuarioUseCase`,
      `UpdateUsuarioUseCase` e `ApproveUsuarioUseCase` verificando que os campos de auditoria são
      populados a partir do actor autenticado `4271d38`
- [x] Task: Implementar a população dos campos de auditoria nos 4 casos de uso (Green) `4271d38`
- [x] Task: Refatorar se necessário — não necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `c1f8179`

  **Desvio descoberto durante a implementação:** tornar `criado_em_utc`/`atualizado_em_utc` `NOT NULL`
  quebrou 37 classes de teste pré-existentes que criam `UsuarioEntity` sem passar pelos 4 use cases
  (fixtures diretas e um fixture SQL raw — `security-fixtures.sql`). Resolvido com `@PrePersist`/
  `@PreUpdate` na entidade (cobre criação via JPA) + `DEFAULT CURRENT_TIMESTAMP` na coluna (cobre INSERT
  via SQL raw). `criadoPor`/`atualizadoPor` permanecem nullable — só os 4 use cases de fato os populam.
  Ver git note do commit `4271d38`.

## Phase 4: Documentação para agentes de IA
- [x] Task: Atualizar `gestao-paroquia/AGENTS.md` (e/ou skill relevante em `.agents/skills/`) descrevendo o
      agregado `Fiel`, o enum `Vocacao`, e a distinção Fiel (pessoa/contato) vs. Usuario (login/credenciais) `248bd84`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Validação final e fechamento da track
- [ ] Task: Rodar a suíte completa (`./gradlew test`) e confirmar 100% de sucesso, sem regressões
- [ ] Task: Verificar cobertura de código do novo pacote `fiel/` e dos trechos alterados em `iam/`
      (target >80%, conforme workflow.md)
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)
