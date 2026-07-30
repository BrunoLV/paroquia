# Implementation Plan: Otimização de Arquitetura Identificada pelo Grafo (Segregação de God Nodes)

Refatoração e segregação dos repositórios God Nodes (`UsuarioJpaRepository`, `EventoJpaRepository`, `OrganizacaoJpaRepository`) e suas injeções de dependência na aplicação `gestao-paroquia`.

## Phase 1: Segregação do Domínio e Repositórios de Usuário (`gestao-paroquia`)

- [ ] Task: Escrever/atualizar testes unitários e de integração para os submódulos de Usuário (TDD Red)
    - [ ] Criar especificações de teste para as novas sub-interfaces (ex: `UsuarioAutenticacaoRepository`, `UsuarioPerfilRepository`).
- [ ] Task: Implementar sub-interfaces de repositório e refatorar injeções de Usuário (TDD Green)
    - [ ] Criar sub-interfaces de repositório focadas para Usuário.
    - [ ] Atualizar injeções de dependência em controllers, services e use cases de Usuário.
    - [ ] Executar `./gradlew test` e validar o módulo de Usuário.
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Segregação do Domínio e Repositórios de Usuário' (Protocol in workflow.md)

## Phase 2: Segregação do Domínio e Repositórios de Evento (`gestao-paroquia`)

- [ ] Task: Escrever/atualizar testes unitários e de integração para os submódulos de Evento (TDD Red)
    - [ ] Criar especificações de teste para as novas sub-interfaces (ex: `EventoConsultaRepository`, `EventoAgendamentoRepository`).
- [ ] Task: Implementar sub-interfaces de repositório e refatorar injeções de Evento (TDD Green)
    - [ ] Criar sub-interfaces de repositório focadas para Evento.
    - [ ] Atualizar injeções de dependência em controllers, services e use cases de Evento.
    - [ ] Executar `./gradlew test` e validar o módulo de Evento.
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Segregação do Domínio e Repositórios de Evento' (Protocol in workflow.md)

## Phase 3: Segregação do Domínio e Repositórios de Organização (`gestao-paroquia`)

- [ ] Task: Escrever/atualizar testes unitários e de integração para os submódulos de Organização (TDD Red)
    - [ ] Criar especificações de teste para as novas sub-interfaces (ex: `OrganizacaoConsultaRepository`, `OrganizacaoGestaoRepository`).
- [ ] Task: Implementar sub-interfaces de repositório e refatorar injeções de Organização (TDD Green)
    - [ ] Criar sub-interfaces de repositório focadas para Organização.
    - [ ] Atualizar injeções de dependência em controllers, services e use cases de Organização.
    - [ ] Executar `./gradlew test` e validar o módulo de Organização.
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Segregação do Domínio e Repositórios de Organização' (Protocol in workflow.md)

## Phase 4: Validação Integrada do Monorepo e Qualidade Estática

- [ ] Task: Executar suíte completa de verificação (`./gradlew check` e `npm test -- --run`)
    - [ ] Executar build, Checkstyle, SpotBugs e suíte completa de testes no backend (`./gradlew check`).
    - [ ] Executar suíte de testes do frontend (`npm test -- --run`) para verificar integração final.
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Validação Integrada do Monorepo e Qualidade Estática' (Protocol in workflow.md)
