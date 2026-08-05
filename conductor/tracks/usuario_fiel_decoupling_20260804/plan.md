# Plan: Desacoplamento de `UsuarioEntity` dos Dados Pessoais do `Fiel`

## Phase 0: Setup
- [x] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      refactor/usuario-fiel-decoupling-<data>`) — branch `refactor/usuario-fiel-decoupling-20260805`
- [x] Task: Confirmar que a Track 1 está implementada na base atual (agregado `Fiel`, `UsuarioEntity.fielId`
      nullable, campos de auditoria) — se não estiver, HALT e avisar o usuário — confirmado: PR #5 mergeado
      em master antes desta branch ser criada
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `df92e64`

## Phase 1: `fielId` obrigatório
- [x] Task: Escrever teste (Red) confirmando que persistir `UsuarioEntity` sem `fielId` falha por violação
      de constraint `NOT NULL` `e5a0874`
- [x] Task: Criar migração Flyway tornando `usuarios.fiel_id` `NOT NULL` (Green) `e5a0874`
- [x] Task: Refatorar se necessário — não necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md) `0030bac`

  **Escopo ampliado (confirmado com o usuário):** a constraint NOT NULL quebrou 8 classes de teste que
  criam `UsuarioEntity` diretamente (além do fixture `security-fixtures.sql`, já usado por ~28 classes,
  e `docker/seed-data.sql`, com 14 usuários de seed). Todos corrigidos no mesmo commit, criando um `Fiel`
  real antes de cada `Usuario` — via novo helper `FielTestFixtures`. `UsuarioRegistrationIntegrationTest`
  foi removido (testava só o autocadastro público, que a Phase 4 desta track já remove). Ver git note do
  commit `e5a0874`.

## Phase 2: Remover dados pessoais de `UsuarioEntity`
- [x] Task: Escrever/ajustar testes (Red) que capturam o comportamento atual de `UsuarioResponse`,
      `MembroMapper` e demais consumidores listados em FR3, usando dados agora vindos de um `Fiel` associado
      (fixtures de teste passam a criar um `Fiel` + `Usuario` vinculado) `67727ce`
- [x] Task: Criar migração Flyway removendo as colunas pessoais (`nome`, `telefone`, `data_nascimento`,
      `batizado`, `primeira_comunhao`, `crismado`, `casado_igreja_catolica`) de `usuarios` (V060) `67727ce`
- [x] Task: Remover os campos e converters correspondentes de `UsuarioEntity` (Green) `67727ce`
- [x] Task: Atualizar `MembroMapper` para resolver nome/telefone via `Fiel` (Green) `67727ce`
- [x] Task: Atualizar `UsuarioResponse`/mapeamento em `UsuarioController`, `UpdateUsuarioUseCase`,
      `GetUsuarioUseCase`, `ListUsuariosUseCase` para resolver via `Fiel`, mantendo o formato de resposta
      (Green) `67727ce`
- [x] Task: Refatorar se necessário — corrigidos também `CreateMembroUseCase`/`UpdateMembroUseCase`
      (sincronização de liderança), `ListOrganizacaoMembrosUseCase` e `OrganizacaoRepositoryImpl` (resolução
      dinâmica de nome de coordenador/vice-coordenador), não listados no FR3 original mas quebrados pela
      remoção dos campos `67727ce`
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

  **Escopo fundido com a Phase 4 (confirmado com o usuário):** `RegistrarUsuarioUseCase` chamava
  diretamente os setters que esta fase remove, então sua remoção (originalmente Phase 4) teve que
  acontecer no mesmo commit — não é possível compilar um estado intermediário onde a Phase 2 está pronta
  mas a Phase 4 não. Ver detalhes completos na Phase 4 abaixo e no git note do commit `67727ce`.

  **Outros dois desvios não previstos:** o import em lote de USUARIO (`ImportacaoService`) também setava
  os mesmos campos e não tinha coluna de CPF (exigido por `Fiel`) — removido, mantendo MEMBRO/ORGANIZACAO/
  EVENTO intocados. E `UsuarioPerfilJpaRepository.findByNomeIgnoreCase`, que buscava pelo `nome`
  criptografado de `Usuario`, nunca funcionava de fato (mesmo problema de IV aleatório do CPF na Track 1) —
  removido como dead code, não como regressão de comportamento.

## Phase 3: Criação de `Usuario` exige `Fiel` existente
- [ ] Task: Escrever testes (Red) para `CreateUsuarioUseCase` cobrindo: sucesso com `fielId` existente,
      falha com `fielId` inexistente
- [ ] Task: Implementar a validação em `CreateUsuarioUseCase` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: Remoção do autocadastro público
- [x] Task: Remover/ajustar testes existentes de `RegistrarUsuarioUseCase`/endpoint `/registrar` (Red —
      confirmar que o endpoint deixa de responder 200/201 e passa a 404) `67727ce`
- [x] Task: Remover `RegistrarUsuarioUseCase`, `RegistrarUsuarioRequest`, o método/mapping `/registrar` em
      `UsuarioController`, e a regra `permitAll()` correspondente em `SecurityConfig` (Green) `67727ce`
- [x] Task: Revisar `ApproveUsuarioUseCase`: ajustar leitura de dados pessoais para vir do `Fiel` associado
      ao `Usuario` pendente, com teste cobrindo o fluxo de aprovação — revisado: nunca leu dados pessoais
      diretamente (só `enabled`/`roles`), nenhuma mudança de código necessária além do ajuste de Javadoc;
      cobertura existente (`ApproveUsuarioUseCaseTest`, da Track 1) permanece válida `67727ce`
- [x] Task: Refatorar se necessário — não necessário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

  **Executada antecipadamente dentro da Phase 2** por dependência técnica rígida — ver nota acima. Esta
  fase fica registrada aqui por completude do plano, mas todo o trabalho já está no commit `67727ce`.

## Phase 5: Documentação para agentes de IA
- [ ] Task: Atualizar `gestao-paroquia/AGENTS.md` (e/ou skill relevante) documentando que `Usuario` não
      carrega mais dados pessoais, que toda criação de `Usuario` parte de um `Fiel` existente, e que o
      autocadastro público foi removido
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 6: Validação final e fechamento da track
- [ ] Task: Rodar a suíte completa (`./gradlew test`) e confirmar 100% de sucesso, sem regressões
- [ ] Task: Verificar cobertura de código dos trechos alterados (target >80%)
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)
