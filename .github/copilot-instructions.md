# Regras de Governança do Projeto Paróquia (.github/copilot-instructions.md)

Todas as diretrizes de arquitetura, padrões de código (Java/React), regras de LGPD, fluxo de trabalho e comandos de ferramentas estão centralizadas para garantir paridade entre todos os agentes de IA operando neste repositório.

**Ações Obrigatórias:**
Para tarefas frontend, leia `gestao-paroquia-ui/AGENTS.md`, `gestao-paroquia-ui/DESIGN_SYSTEM.md` e `.agents/rules/frontend-design-system.md`. Toda lista de valores, inclusive filtros, seleção múltipla e Linhas por página, oferece autocomplete com pesquisa conforme a seção 2.1 do Design System.
1. Leia o arquivo `AGENTS.md` localizado na raiz deste repositório para compreender a arquitetura e diretrizes.
2. Cumpra rigorosamente a regra ALWAYS-ON em `.agents/rules/fluxo-desenvolvimento.md`:
   - No planejamento: Consulte as regras do módulo em `docs/business-rules/<modulo>.md`.
   - Na execução: Utilize GitHub Flow com branch semântica saindo de `master` atualizada.
   - Atualize `docs/business-rules/<modulo>.md` quando alterar negócio; diretivas técnicas e visuais ficam na documentação técnica e no Design System.
   - Questione o fechamento de versão e mantenha `CHANGELOG.md`.
   - Submeta alterações exclusivamente via Pull Request formatado para a `master`.

👉 **Leia `AGENTS.md` e `.agents/rules/fluxo-desenvolvimento.md` agora.**
