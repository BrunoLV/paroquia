# CLAUDE.md

Todas as instruções, regras de governança, comandos de ferramentas e padrões arquiteturais deste repositório (Projeto Paróquia) foram unificados para manter consistência absoluta entre qualquer agente de IA (Claude, Gemini, Antigravity, Cursor, Copilot).

**Ações Obrigatórias e Inegociáveis:**
1. Antes de explorar a base de código, planejar ou propor modificações, você DEVE LER o arquivo `AGENTS.md` localizado na raiz deste repositório.
2. Você DEVE seguir estritamente o fluxo permanente definido em `.agents/rules/fluxo-desenvolvimento.md` e na skill `.agents/skills/fluxo-desenvolvimento/SKILL.md`:
   - No modo Planejamento (Plan Mode): Consultar `docs/business-rules/<modulo>.md` e obter aprovação prévia.
   - Na Execução: Usar GitHub Flow (`master` atualizada -> branch semântica), atualizar a documentação em `docs/business-rules/`, questionar sobre fechamento de versão, manter `CHANGELOG.md` e submeter via Pull Request para a `master`.

👉 **Leia `AGENTS.md` agora para carregar seu contexto de operação.**