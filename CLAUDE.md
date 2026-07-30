# CLAUDE.md / AI Agent Rules

## MANDATO OBRIGATÓRIO DO GRAPHIFY E ECONOMIA DE TOKENS

1. **Uso Primário dos Grafos de Conhecimento (`graphify-out/`):**
   - A pasta `graphify-out/` está presente na raiz da aplicação contendo a análise AST completa, grafo de dependências (`graph.json`), relatório de arquitetura (`GRAPH_REPORT.md`) e mapeamento de comunidades e god nodes.
   - Qualquer agente de IA atuando neste repositório DEVE consultar prioritariamente os artefatos do `graphify` para mapear dependências, encontrar classes/componentes afetados e entender a arquitetura do projeto.

2. **Proibição de Leitura em Massa (Token Optimization):**
   - É estritamente proibido fazer leituras cegas de múltiplos arquivos completos (`view_file` em massa) sem antes usar o grafo para isolar exatamente os alvos de modificação.
   - O agente deve ler apenas as linhas/trechos específicos dos arquivos identificados via Graphify.

3. **Governança de Código e Imports:**
   - Proibido usar FQN inline imports em Java (ex: `br.com.nsfatima...LiturgicalEngine`). Usar sempre declarações `import` no topo do arquivo.
   - Respeitar os guias de arquitetura hexagonal/DDD no backend e Domain-Driven UI no frontend (`AGENTS.md`).
