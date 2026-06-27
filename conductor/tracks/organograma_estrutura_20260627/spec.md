# Specification: Visualização da Estrutura Paroquial (Organograma)

## Overview
Esta funcionalidade visa fornecer uma visão clara, visual e interativa da estrutura organizacional da paróquia. Ela permitirá que qualquer usuário autenticado compreenda as relações de liderança e subordinação, desde o pároco/clero no topo até os membros vinculados de cada pastoral e movimento.

## Functional Requirements
1. **Página Dedicada ("Organograma"):**
   - Criar uma nova rota no frontend `/organograma` e adicioná-la ao menu de navegação lateral (Sidebar) para todos os usuários autenticados.
2. **Hierarquia Organizacional:**
   - O organograma deve estruturar os dados de forma hierárquica:
     - **Nível 1 (Topo):** Clero (Pároco/Vigário/Padres).
     - **Nível 2:** Conselho Paroquial (Coordenador, Vice e Secretário do Conselho).
     - **Nível 3:** Organizações locais (Pastorais, Movimentos, Laicato, etc.), mostrando seu respectivo Coordenador e Vice-Coordenador.
     - **Nível 4:** Membros ativos associados a cada organização local.
3. **Árvore Gráfica Interativa:**
   - Renderizar a hierarquia graficamente em formato de árvore usando nós e linhas conectivas (usando SVG ou biblioteca compatível).
   - Nós de organizações e clero devem destacar seus respectivos nomes e papéis (ex: Pe. Pedro da Silva - Pároco).
   - Nós de organizações com muitos membros devem permitir expansão/colapso para evitar poluição visual.
4. **Integração com a API Backend:**
   - Criar um endpoint `/api/v1/organizacoes/organograma` que retorne a árvore estruturada contendo todos os nós da hierarquia paroquial de forma otimizada para evitar N+1 queries.
5. **Base de Testes e Dados de Seed:**
   - Garantir que a base de testes (`seed-data.sql` e bases de testes unitários) contenha registros consistentes abrangendo todos os níveis da hierarquia (Clero, Conselho, Pastorais e Membros ativos) para que a tela seja renderizada completa e corretamente imediatamente após subir o ambiente.

## Non-Functional Requirements
- **Desempenho:** A árvore deve carregar em menos de 1 segundo para o volume atual de organizações (~10 organizações e ~50 membros).
- **Responsividade:** O diagrama deve ser navegável em telas menores através de gestos de pan/zoom ou barras de rolagem adequadas.

## Acceptance Criteria
- A página do Organograma é acessível a qualquer usuário logado.
- A árvore renderiza o Clero no topo, seguido pelo Conselho, depois as pastorais com seus coordenadores, e finalmente a lista de membros de cada pastoral.
- É possível expandir/colapsar os membros de uma pastoral/movimento para melhor visualização.
- A base de dados de testes vem pré-populada com dados de hierarquia completos e coerentes.
- Todos os testes de integração do novo endpoint e componentes visuais passam com sucesso.

## Out of Scope
- Edição direta de membros ou lideranças clicando nos nós do organograma (a edição continua sendo feita exclusivamente na tela de Configurações).
