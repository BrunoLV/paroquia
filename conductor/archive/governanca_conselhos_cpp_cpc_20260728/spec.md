# Specification: Governança dos Conselhos (CPP e CPC) e Homologação de Atas

## Overview
Implementação da governança eclesial dividida entre o Conselho Pastoral Paroquial (CPP - Deliberativo/Global) e o Conselho Pastoral Comunitário (CPC - Operacional/Local), incluindo o fluxo completo (Backend e Frontend UI) de submissão, homologação, acompanhamento e veto de atas/decisões pelo Pároco ou pela mesa do CPP.

## Functional Requirements

### Backend & API
1. **Entidades e Persistência:** Criar a tabela `cpc_decisoes` via Flyway `V047` e a entidade `CpcDecisaoEntity`, controlando o ciclo de vida: `PENDENTE_HOMOLOGACAO`, `HOMOLOGADO`, `VETADO`.
2. **Workflow de Homologação:** Endpoint `/api/v1/conselhos/decisoes/{id}/homologacao` acessível apenas para usuários autenticados com o papel `PAROCO`, `ADMIN` ou `MESA_CPP` para homologar ou vetar (exigindo justificativa em caso de veto).
3. **Filtros e Consultas:** API de listagem de decisões com suporte a filtros por comunidade (`community_id`), status de homologação e tipo de conselho (CPP/CPC).

### Frontend (UI)
4. **Tela de Conselhos & Atas (`ConselhosAtas.tsx`):**
   - Página dedicada à gestão de decisões e atas dos conselhos (CPP e CPC), integrada ao menu do sistema.
   - Tabela responsiva com badges visuais de status (`PENDENTE_HOMOLOGACAO`, `HOMOLOGADO`, `VETADO`), ordenação por data da reunião e filtros por comunidade/status.
5. **Modal de Submissão de Decisões/Atas (`CpcDecisaoModal.tsx`):**
   - Formulário para cadastro de nova decisão com campos: Título da Ata, Data da Reunião, Comunidade (CPC de origem), Pauta/Deliberação e Resumo.
6. **Ações de Homologação e Veto (Pároco & Mesa CPP):**
   - Botões de ação direta ("Homologar", "Vetar") visíveis e habilitados apenas para Pároco, Admin e Mesa CPP.
   - Modal/Dialog de veto com campo de justificativa obrigatório para envio do motivo de reprovação.
7. **Serviços e Testes React:**
   - Módulo de integração de API (`conselhosApi.ts`) e cobertura por testes unitários e de componente em Vitest (`ConselhosAtas.test.tsx`).

## Acceptance Criteria
- [ ] Atas e decisões registradas são gravadas como `PENDENTE_HOMOLOGACAO` por padrão.
- [ ] Tentativas de alteração de status por usuários sem permissão de `PAROCO`, `ADMIN` ou `MESA_CPP` retornam erro 403 (Backend e UI).
- [ ] Tela de "Conselhos & Atas" operacional no menu da UI, permitindo submissão por membros dos CPCs e homologação/veto pelos perfis autorizados.
- [ ] Veto exige justificativa obrigatória no Frontend e no Backend.
- [ ] Suíte completa de testes no Backend (`./gradlew test`) e Frontend (`npm test`) sem regressões.
