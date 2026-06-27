# Implementation Plan: Visualização da Estrutura Paroquial (Organograma)

## Phase 1: Database Seed and Backend API Endpoint (TDD)
- [ ] Task: Garantir a consistência dos dados de seed para a hierarquia [TDD]
    - [ ] Atualizar o script `seed-data.sql` para garantir que o Pároco, Conselho e pelo menos duas pastorais/movimentos com coordenadores, vice-coordenadores e membros ativos estejam populados e vinculados
- [ ] Task: Desenvolver o endpoint de dados do organograma no backend [TDD]
    - [ ] Criar DTOs para representação da árvore do organograma (ex: `OrganogramaNodeResponse`)
    - [ ] Criar classe de serviço/caso de uso `GetOrganogramaUseCase` para construir a hierarquia estruturada de forma otimizada
    - [ ] Criar endpoint `GET /api/v1/organizacoes/organograma` em `OrganizacaoController` (acesso liberado para qualquer autenticado `hasRole('USER')`)
- [ ] Task: Escrever testes unitários e de integração no backend e rodar `./gradlew test` [TDD]
    - [ ] Criar testes de integração do endpoint em `OrganizacaoController` validando o retorno estruturado da árvore de hierarquia
    - [ ] Criar testes unitários para a montagem de nós da árvore no caso de uso
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Database Seed and Backend API Endpoint (TDD)' (Protocol in workflow.md)

## Phase 2: Frontend Layout and Organograma Interactive View (TDD)
- [ ] Task: Implementar as chamadas de API no frontend
    - [ ] Adicionar método `getOrganograma` no arquivo `src/api/organizacoes.ts`
- [ ] Task: Criar a página de visualização do Organograma
    - [ ] Criar novo componente/página `src/pages/Organograma.tsx`
    - [ ] Adicionar rota `/organograma` no `App.tsx`
    - [ ] Adicionar atalho/item "Organograma" no menu lateral (Sidebar) visível a qualquer usuário autenticado
- [ ] Task: Implementar a árvore interativa no frontend
    - [ ] Desenhar o organograma usando SVG ou biblioteca de diagramas leve (compatível com os padrões de design premium do projeto)
    - [ ] Customizar styles dos nós do diagrama para destacar Clero, Conselho, Pastorais e Membros
    - [ ] Implementar recurso de expandir/contrair lista de membros associados a pastorais
- [ ] Task: Escrever testes automatizados do frontend e rodar testes/build
    - [ ] Criar arquivo de testes `src/pages/Organograma.test.tsx` cobrindo a renderização dos nós da árvore e interações
    - [ ] Validar a compilação geral rodando `npm run build` e `CI=true npm test` no frontend
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Frontend Layout and Organograma Interactive View (TDD)' (Protocol in workflow.md)
