# Plan: Desacoplamento de `UsuarioEntity` dos Dados Pessoais do `Fiel`

## Phase 0: Setup
- [ ] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      refactor/usuario-fiel-decoupling-<data>`)
- [ ] Task: Confirmar que a Track 1 está implementada na base atual (agregado `Fiel`, `UsuarioEntity.fielId`
      nullable, campos de auditoria) — se não estiver, HALT e avisar o usuário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 1: `fielId` obrigatório
- [ ] Task: Escrever teste (Red) confirmando que persistir `UsuarioEntity` sem `fielId` falha por violação
      de constraint `NOT NULL`
- [ ] Task: Criar migração Flyway tornando `usuarios.fiel_id` `NOT NULL` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 2: Remover dados pessoais de `UsuarioEntity`
- [ ] Task: Escrever/ajustar testes (Red) que capturam o comportamento atual de `UsuarioResponse`,
      `MembroMapper` e demais consumidores listados em FR3, usando dados agora vindos de um `Fiel` associado
      (fixtures de teste passam a criar um `Fiel` + `Usuario` vinculado)
- [ ] Task: Criar migração Flyway removendo as colunas pessoais (`nome`, `telefone`, `data_nascimento`,
      `batizado`, `primeira_comunhao`, `crismado`, `casado_igreja_catolica`) de `usuarios`
- [ ] Task: Remover os campos e converters correspondentes de `UsuarioEntity` (Green)
- [ ] Task: Atualizar `MembroMapper` para resolver nome/telefone via `Fiel` (Green)
- [ ] Task: Atualizar `UsuarioResponse`/mapeamento em `UsuarioController`/`UsuarioAdminController`,
      `UpdateUsuarioUseCase`, `GetUsuarioUseCase`, `ListUsuariosUseCase` para resolver via `Fiel`, mantendo o
      formato de resposta (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 3: Criação de `Usuario` exige `Fiel` existente
- [ ] Task: Escrever testes (Red) para `CreateUsuarioUseCase` cobrindo: sucesso com `fielId` existente,
      falha com `fielId` inexistente
- [ ] Task: Implementar a validação em `CreateUsuarioUseCase` (Green)
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: Remoção do autocadastro público
- [ ] Task: Remover/ajustar testes existentes de `RegistrarUsuarioUseCase`/endpoint `/registrar` (Red —
      confirmar que o endpoint deixa de responder 200/201 e passa a 404)
- [ ] Task: Remover `RegistrarUsuarioUseCase`, `RegistrarUsuarioRequest`, o método/mapping `/registrar` em
      `UsuarioController`, e a regra `permitAll()` correspondente em `SecurityConfig` (Green)
- [ ] Task: Revisar `ApproveUsuarioUseCase`: ajustar leitura de dados pessoais para vir do `Fiel` associado
      ao `Usuario` pendente, com teste cobrindo o fluxo de aprovação
- [ ] Task: Refatorar se necessário
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Documentação para agentes de IA
- [ ] Task: Atualizar `gestao-paroquia/AGENTS.md` (e/ou skill relevante) documentando que `Usuario` não
      carrega mais dados pessoais, que toda criação de `Usuario` parte de um `Fiel` existente, e que o
      autocadastro público foi removido
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 6: Validação final e fechamento da track
- [ ] Task: Rodar a suíte completa (`./gradlew test`) e confirmar 100% de sucesso, sem regressões
- [ ] Task: Verificar cobertura de código dos trechos alterados (target >80%)
- [ ] Task: Phase Verification & Checkpoint (Refer to workflow.md)
