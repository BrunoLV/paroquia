# Specification: Governança dos Conselhos (CPP e CPC) e Homologação de Atas

## Overview
Implementação da governança eclesial dividida entre o Conselho Paroquial Paroquial (CPP - Deliberativo/Global) e o Conselho Pastoral Comunitário (CPC - Operacional/Local), incluindo o fluxo obrigatório de submissão, homologação e veto de atas/decisões pelo Pároco ou pela mesa do CPP.

## Functional Requirements
1. **Entidades de Atas/Decisões de CPC:** Criar a entidade e repositório de Decisões de CPC com o ciclo de vida: `PENDENTE_HOMOLOGACAO`, `HOMOLOGADO`, `VETADO`.
2. **Workflow de Homologação:** Permitir que apenas usuários autenticados com o papel `PAROCO` ou membros da mesa do `CPP` possam aprovar (`HOMOLOGADO`) ou vetar (`VETADO`) decisões registradas pelos CPCs locais.
3. **Validação contra Diretrizes Macro:** Impedir a criação ou modificação de atividades locais que violem diretrizes cadastradas no módulo global do CPP.

## Acceptance Criteria
- [ ] Atas e decisões de CPC são salvas por padrão como `PENDENTE_HOMOLOGACAO`.
- [ ] Usuários sem papel `PAROCO` ou autorização de `CPP` recebem erro de acesso ao tentar alterar o status para `HOMOLOGADO`.
- [ ] Tabela e endpoints de decisões de CPC estão operacionais e cobertos por testes de integração.
