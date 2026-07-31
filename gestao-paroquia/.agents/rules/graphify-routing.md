---
Name: Graphify Token Conservation Routing
Activation: Always On
---

# Diretrizes Críticas de Economia de Contexto

Você DEVE agir estritamente sob estas restrições de segurança e orçamento para evitar o desperdício de tokens:

1. **Proibição de Varredura Bruta (Anti-Grep):** Você está PROIBIDO de usar comandos textuais em lote como `grep`, `find` ou buscas lineares recursivas no repositório para entender a arquitetura do projeto.
2. **Prioridade Máxima do Grafo:** Antes de responder a qualquer prompt ou iniciar tarefas de codificação, você DEVE inspecionar os arquivos gerados pelo Graphify em `graphify-out/` ou ler o resumo compactado em `GRAPH_REPORT.md`.
3. **Resolução de Dependências por Nós:** Identifique a localização de componentes, rotas e regras de negócio mapeando as conexões do `graph.json`. Você só tem permissão para carregar o conteúdo de um arquivo de código para o contexto ativo se ele estiver listado no caminho direto do nó relacionado à tarefa atual.
4. **Alocação de Subagentes:** Ao disparar múltiplos agentes em paralelo usando `/agents`, instrua-os a herdar as informações do grafo em vez de forçá-los a reindexar ou reler o repositório do zero.
5. **Aviso de Quebra de Regra:** Se os artefatos do Graphify estiverem ausentes ou desatualizados, exorte o usuário a rodar o comando `/graphify .` em vez de tentar ler os arquivos brutos diretamente.
