# Implementation Plan: RBAC Eclesial e Governança Paroquial (CPP & CAE)

## Phase 1: Banco de Dados, Domínio Backend & Fluxo de Aprovações
- [ ] Task: Criar script de migração Flyway e atualizar scripts/seed-data para adequação de tabelas do modelo de dados (`papel`, `tipo_organizacao`, roles de usuários e membros)
- [ ] Task: Atualizar enums de domínio (`PapelOrganizacional.java`, `TipoOrganizacao.java`, `AprovadorPapel.java`) com os papéis eclesiais oficiais
- [ ] Task: Ajustar o sistema de permissões e serviços de autorização/aprovação (`EventoAprovacaoService`, `EventoCancelamentoAuthorizationService`, `EventoPatchAuthorizationService`) para os novos papéis eclesiais
- [ ] Task: Atualizar suíte de testes unitários e de integração do backend e validar com `./gradlew check`
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Backend & Database' (Protocol in workflow.md)

## Phase 2: Permissões Frontend & Padronização de Nomenclatura (UI)
- [ ] Task: Atualizar `src/api/types.ts` e `src/auth/permissions.ts` para mapear o termo "Organismo Paroquial" e as novas roles eclesiais (CPP, CAE, Bispo, Diácono, etc.)
- [ ] Task: Padronizar nomes e rótulos oficiais ("Conselho Pastoral Paroquial (CPP)", "Conselho Econômico Paroquial (CAE)", "Organismo Paroquial") em `Organograma.tsx`, `Settings.tsx` e `OrganizacaoFormModal.tsx`
- [ ] Task: Atualizar testes frontend em `permissions.test.ts`, `Organograma.test.tsx`, `Settings.test.tsx` e `OrganizacaoFormModal.test.tsx`
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Frontend Permissions & UI' (Protocol in workflow.md)

## Phase 3: Validação Completa da Aplicação & Regressão Zero
- [ ] Task: Executar validação E2E com `./gradlew check` no backend e `npm run build && npm test` no frontend garantindo zero regressões
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Final Verification' (Protocol in workflow.md)
