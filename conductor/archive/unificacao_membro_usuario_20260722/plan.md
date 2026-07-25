# Implementation Plan: Unificação do Conceito de Membro e Usuário (Membro como Vínculo Organizacional)

## Phase 1: Backend Domain & Persistence Refactoring (Java / Spring Boot)

- [x] Task 1.1: Atualizar modelo de domínio `Usuario` e `UsuarioEntity` com os 4 campos sacramentais booleanos (`batizado`, `primeiraComunhao`, `crismado`, `casadoIgrejaCatolica`) <!-- id: task-1.1 -->
- [x] Task 1.2: Refatorar modelo de domínio `Membro` e `MembroEntity` removendo atributos pessoais duplicados e tornando `usuarioId`, `organizacaoId` e `papel` estritamente obrigatórios <!-- id: task-1.2 -->
- [x] Task 1.3: Criar script Flyway/migration e atualizar seeds para suportar o novo schema e migrar membros legados para a tabela `usuarios` <!-- id: task-1.3 -->

## Phase 2: Backend Use Cases, Regras de Negócio e Importação em Lote

- [x] Task 2.1: Atualizar use cases de CRUD de Membro (`CreateMembroUseCase`, `UpdateMembroUseCase`, `GetMembroUseCase`) adicionando validação de vínculo único e sincronização automática de liderança com `Organizacao` <!-- id: task-2.1 -->
- [x] Task 2.2: Atualizar motor de importação em lote (CSV/Excel) para separar importação de Usuários (dados pessoais e sacramentais) da importação de Membros (vínculos organizacionais com validação de existência de usuário) <!-- id: task-2.2 -->
- [x] Task 2.3: Atualizar e validar suíte de testes unitários e de integração no backend (`./gradlew test`) <!-- id: task-2.3 -->


## Phase 3: Frontend Refactoring (React + TypeScript)

- [x] Task 3.1: Atualizar interfaces TypeScript (`Usuario`, `Membro`, DTOs) em `gestao-paroquia-ui` <!-- id: task-3.1 -->
- [x] Task 3.2: Atualizar modal/formulário de Usuário (`UsuarioFormModal`) para suporte aos 4 checkboxes sacramentais e edição dos dados do fiel <!-- id: task-3.2 -->
- [x] Task 3.3: Refatorar modal/listagem de Membros (`MembroFormModal` / `MembrosPage`) para seleção de `Usuario` existente via autocompletar + `Organizacao` + `Papel` <!-- id: task-3.3 -->
- [x] Task 3.4: Atualizar `OrganizacaoFormModal` / `Organograma` para vincular e exibir Coordenador e Vice-Coordenador a partir dos membros associados <!-- id: task-3.4 -->
- [x] Task 3.5: Alinhar tela de cadastro público de Usuário na página de Login (`Login.tsx`) estritamente com a dinâmica de cadastro gerencial (removendo seleção de pastorais desejadas e mantendo dados do fiel, igreja/comunidade e indicadores sacramentais) <!-- id: task-3.5 -->

## Phase 4: Validação Integrada e Conclusão


- [x] Task 4.1: Validar compilação e suíte de testes no frontend (`npm test` e `npm run build`) <!-- id: task-4.1 -->
- [x] Task 4.2: Executar testes de integração end-to-end do fluxo de Usuário, Membro e Organização <!-- id: task-4.2 -->

## Phase 5: Review Fixes & Adjustments

- [x] Task: Apply review suggestions and seed resiliency fixes 2aa9fad <!-- id: task-5.1 -->
