# Specification: Vínculos de Membros com Organizações (Consistência Bidirecional e Sincronismo de Liderança)

## Overview
Esta especificação define o ajuste completo da regra de negócios e persistência para vínculos de membros e usuários com organizações na paróquia. O objetivo é assegurar consistência bidirecional: os vínculos devem ser perfeitamente visualizados e gerenciados tanto a partir do perfil do Membro/Usuário quanto a partir da tela da Organização (Estrutura Paroquial / Pastoral). Além disso, a definição de cargos de liderança (Coordenador e Vice-Coordenador) deve sincronizar os vínculos correspondentes na tabela de membros de organização, e a carga inicial `docker/seed-data.sql` deve ser atualizada para refletir essa estrutura.

## Functional & Technical Requirements

1. **Consulta Bidirecional de Membros de Organização (Backend Java)**:
   - O endpoint `GET /api/v1/organizacoes/{id}/membros` deve retornar a lista consolidada de todos os vínculos ativos pertencentes àquela organização.
   - Para registros vinculados a uma conta de usuário (`usuarioId`), o caso de uso deve resolver o nome completo e dados a partir da entidade `UsuarioEntity`.
   - Para registros vinculados a um membro cadastrado (`membroId`), o caso de uso deve resolver os dados a partir da entidade `MembroEntity`.
   - O DTO de resposta deve incluir: `id` do vínculo, `organizacaoId`, `usuarioId`, `membroId`, `nomeCompleto`, `papel`, `tipo`, `ativo` e `igrejaVinculoId`.

2. **Sincronismo de Liderança (Coordenador / Vice-Coordenador)**:
   - Ao definir ou alterar o Coordenador ou Vice-Coordenador no cadastro de uma Organização, o backend deve garantir que exista um registro ativo correspondente em `MembroOrganizacaoEntity` com o papel e tipo adequados (`COORDENADOR`, `VICE_COORDENADOR`).

3. **Interface Frontend (React / Settings / ParoquiaStructure)**:
   - Na tela de Estrutura Paroquial e Configurações, o modal **Membros da Organização** deve exibir corretamente a lista completa de pessoas (usuários e membros leigos) com seus respectivos papéis.
   - O seletor e ação de inclusão de novos membros/líderes por organização deve atualizar a listagem e os papéis imediatamente.

4. **Seed Docker (`docker/seed-data.sql`)**:
   - Atualizar a carga SQL inicial para que todas as pastorais/movimentos possuam coordenadores/vice-coordenadores com registros equivalentes em `membros_organizacao`, respeitando rigorosamente a regra de escopo por mesma igreja/comunidade.

## Non-Functional Requirements
- **Desempenho & Integridade**: Consultas otimizadas sem loops N+1 para resolução de nomes de usuários e membros.
- **Zero Regressão**: Manter 100% dos testes unitários e de integração passing.

## Acceptance Criteria
- Todos os testes do backend (`./gradlew test`) e do frontend (`npm test` e `npm run build`) continuam passando com 100% de sucesso.
- O endpoint `GET /api/v1/organizacoes/{id}/membros` retorna o nome correto para vínculos originados tanto de `UsuarioEntity` quanto de `MembroEntity`.
- Modal de membros por organização no frontend exibe nomes e papéis de todos os vinculados sem células vazias.
- O arquivo `docker/seed-data.sql` carrega os vínculos de liderança e membros sem erros de integridade ou divergências de igreja.
