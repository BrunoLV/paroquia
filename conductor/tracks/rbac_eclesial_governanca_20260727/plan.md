# Implementation Plan: RBAC Eclesial, Governança Paroquial e Motor da Agenda (CPP & CAE)

## Phase 1: Banco de Dados, Domínio Backend & Validador de Eventos
- [x] Task: Criar script de migração Flyway e atualizar scripts/seed-data para adequação de tabelas do modelo de dados (`papel`, `tipo_organizacao`, roles, setores pastorais e campos de eventos `nivel_prioridade`, `autorizado_pelo_cpp`)

- [x] Task: Atualizar enums de domínio (`PapelOrganizacional.java`, `TipoOrganizacao.java`, `CategoriaEvento.java`, `AprovadorPapel.java`) com papéis e setores eclesiais oficiais
- [x] Task: Implementar o motor de validação da agenda em `EventoValidationService` / `EventoAprovacaoService` aplicando os Níveis 1 a 4, bloqueio automático de Solenidades Nível 1 e regra `autorizadoPeloCpp` para Movimentos
- [x] Task: Atualizar suíte de testes unitários e de integração do backend e validar com `./gradlew check`
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend & Event Engine' (Protocol in workflow.md)

## Phase 2: Interface Frontend, Permissões & Cadastro de Eventos (UI)
- [x] Task: Atualizar `src/api/types.ts` e `src/auth/permissions.ts` para mapear "Organismo Paroquial", novos papéis eclesiais e flags de validação de eventos (`nivelPrioridade`, `autorizadoPeloCpp`)
- [x] Task: Padronizar nomes e rótulos oficiais ("Conselho Pastoral Paroquial (CPP)", "Conselho Econômico Paroquial (CAE)", "Organismo Paroquial") e incluir a indicação de prioridade/setores em `Organograma.tsx`, `Settings.tsx`, `OrganizacaoFormModal.tsx` e modal de eventos (`EventRegistrationModal.tsx`)
- [x] Task: Atualizar suíte de testes frontend em `permissions.test.ts`, `Organograma.test.tsx`, `Settings.test.tsx`, `EventRegistrationModal.test.tsx` e `OrganizacaoFormModal.test.tsx`
- [x] Task: Conductor - User Manual Verification 'Phase 2: Frontend UI & Event Modal' (Protocol in workflow.md)


## Phase 3: Validação Completa da Aplicação & Regressão Zero
- [x] Task: Execução do suite completo de testes backend (`./gradlew check`) e frontend (`npm run build && npm test`) confirmando zero regressões
- [x] Task: Conductor - User Manual Verification 'Phase 3: Final Verification' (Protocol in workflow.md)

