# Specification: RBAC Eclesial Específico (Eliminação de Papéis Genéricos)

## Overview
Reformulação do modelo de permissões e autorização (RBAC) do sistema de gestão paroquial para eliminar o uso de papéis genéricos (`COORDENADOR`, `VICE_COORDENADOR`, `MEMBRO`) e adotar exclusivamente papéis eclesiais específicos (`COORDENADOR_PASTORAL_GLOBAL`, `COORDENADOR_PASTORAL_LOCAL`, `COORDENADOR_MOVIMENTO`, `PRESIDENTE_CPC`, `MEMBRO_CPC`, `MEMBRO_CPP`, `VOLUNTARIO_PASTORAL_LOCAL`, `MEMBRO_MOVIMENTO`).

## Functional Requirements
1. **Modelos de Domínio:** Refatorar `PapelOrganizacional` e `TipoOrganizacao` para remover papéis genéricos e introduzir papéis específicos com semântica de autorização bem definida.
2. **Serviços de Autorização:** Atualizar `UserScopeAuthorizationService`, `AuthorizationPolicy`, `EventoPatchAuthorizationService`, `ValidateAprovacaoUseCase` e `AddMembershipUseCase` para avaliar estritamente os papéis específicos.
3. **Validação de Cadastro:** Rejeitar papéis genéricos em requisições DTO da API.
4. **Migração do Banco de Dados:** Migração SQL (Flyway) para converter registros de usuários/membros com papéis genéricos legados para os novos papéis específicos.

## Acceptance Criteria
- [ ] O enum `PapelOrganizacional` não contém mais os valores genéricos `COORDENADOR`, `VICE_COORDENADOR` e `MEMBRO`.
- [ ] Todos os serviços de autorização compilam e passam nos testes de unidade e integração avaliando apenas os novos papéis específicos.
- [ ] Migração SQL executa com sucesso via `./gradlew test`.
