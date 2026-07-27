# Specification: RBAC Eclesial e Governança Paroquial (CPP & CAE)

## Overview
Implementação da governança eclesial e padronização de papéis (RBAC) com suporte completo às nomenclaturas oficiais da Igreja Católica (Código de Direito Canônico / CNBB). Esta track padroniza os termos eclesiais para os **Organismos Paroquiais** (Conselho Pastoral Paroquial - CPP, Conselho Econômico Paroquial - CAE, Clero, Pastorais Orgânicas e Movimentos Vinculados) e unifica os papéis e aprovações no sistema.

## Functional Requirements

### 1. Nomenclaturas Oficiais dos Organismos Paroquiais
- Padronizar nas telas (Organograma, Cadastro de Organismos, Configurações, Permissões) e mensagens do sistema as nomenclaturas eclesiais oficiais:
  - Substituir o termo genérico "Organização" por **"Organismo Paroquial"**.
  - **Conselho Pastoral Paroquial (CPP)**
  - **Conselho Econômico Paroquial (CAE)**

### 2. Reformulação de Enums e Papéis (IAM & Organismos Paroquiais)
- **`PapelOrganizacional` & IAM Roles:** Padronizar os papéis do sistema para cobrir toda a hierarquia eclesial:
  - `BISPO_DIOCESANO`: Auditoria e governança máxima diocesana.
  - `PAROCO`: Administrador máximo da paróquia e capelas, palavra final deliberativa.
  - `VIGARIO`: Assistente do clero paroquial.
  - `DIACONO`: Assistente de ordem sacra.
  - `MEMBRO_CPP`: Membro do Conselho Pastoral Paroquial (supervisão pastoral e planejamento).
  - `MEMBRO_CAE`: Membro do Conselho Econômico Paroquial (gestão financeira e fiscal).
  - `COORDENADOR_PASTORAL`: Liderança de Pastoral Orgânica local.
  - `COORDENADOR_MOVIMENTO`: Liderança de Movimento Eclesial Vinculado.
  - `MEMBRO_PASTORAL`: Membro integrado a uma pastoral.
  - `MEMBRO_MOVIMENTO`: Membro participante de um movimento.
  - `SECRETARIO`: Apoio administrativo paroquial.
- **`TipoOrganizacao` / Tipos de Organismo Paroquial:** Garantir alinhamento dos tipos de organismos no backend e frontend:
  - `MATRIZ`, `COMUNIDADE`, `CLERO`, `CONSELHO_PASTORAL` (CPP), `CONSELHO_ECONOMICO` (CAE), `PASTORAL`, `MOVIMENTO`, `OUTRO`.

### 3. Matriz de Autoridade, Banco de Dados e Permissões Frontend/Backend
- Atualizar tabelas, migrações de banco de dados e a matriz de permissões (`permissions.ts` no UI e autorização no Backend):
  - **Pároco:** Palavra final deliberativa em eventos, projetos e governança.
  - **CPP:** Visão e validação consultiva do planejamento anual, projetos paroquiais e calendário comunitário.
  - **Coordenadores de Pastoral/Movimento:** Gestão de eventos, escalas e membros de seus respectivos Organismos Paroquiais.

## Non-Functional Requirements
- **Zero Regressão:** Manter 100% de aprovação na suíte de testes de backend (`./gradlew check`) e frontend (`npm test`).

## Acceptance Criteria
- [ ] O enum `PapelOrganizacional`, `TipoOrganizacao` e as Roles IAM contêm todos os papéis eclesiais padronizados.
- [ ] As telas de Organograma, Formulário de Organismos e Configurações exibem exclusivamente os rótulos oficiais "Conselho Pastoral Paroquial (CPP)", "Conselho Econômico Paroquial (CAE)" e "Organismo Paroquial".
- [ ] Os serviços de aprovação e autorização no backend e o helper `permissions.ts` no frontend reconhecem os novos papéis eclesiais.
- [ ] Testes unitários e de integração validam as novas permissões e normalizações de enums.

## Out of Scope
- Módulo Financeiro e de Dízimo (postergado para track futura).
- Cruzamento de CEP/Geolocalização para vínculo de paroquianos.
