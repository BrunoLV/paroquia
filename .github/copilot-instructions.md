# Regras de Governança do Projeto Paróquia (.github/copilot-instructions.md)

Todas as diretrizes de arquitetura, padrões de código (Java/React), regras de LGPD, fluxo de trabalho e comandos de ferramentas estão centralizadas para garantir paridade entre todos os agentes de IA operando neste repositório.

**Ações Obrigatórias:**
Para tarefas frontend, leia `gestao-paroquia-ui/AGENTS.md`, `gestao-paroquia-ui/DESIGN_SYSTEM.md` e `.agents/rules/frontend-design-system.md`. Toda lista de valores, inclusive filtros, seleção múltipla e Linhas por página, oferece autocomplete com pesquisa conforme a seção 2.1 do Design System.

Paginação segue a seção 7: `SearchableTablePagination` dentro do `Table`, após `TableBody`, com `colSpan` das colunas visíveis. Seletor Linhas por página e anterior/próximo com mesma altura; estilo compacto exclusivo da paginação, sem afetar formulários. Proibido rodapé externo à tabela; validar `tablePaginationArchitecture.test.ts`.

Ações de linha usam exclusivamente `TableActionMenu`, inclusive ação única: botão pequeno delineado com `⋮` vertical, sem texto visível, tooltip/nome acessível “Ações”. Preservar permissões e estados; executar `tableActionsArchitecture.test.ts` e testes de teclado/foco do menu.
1. Leia o arquivo `AGENTS.md` localizado na raiz deste repositório para compreender a arquitetura e diretrizes.
2. Cumpra rigorosamente a regra ALWAYS-ON em `.agents/rules/fluxo-desenvolvimento.md`:
   - No planejamento: Consulte as regras do módulo em `docs/business-rules/<modulo>.md`.
   - Na execução: Utilize GitHub Flow com branch semântica saindo de `master` atualizada.
   - Atualize `docs/business-rules/<modulo>.md` quando alterar negócio; diretivas técnicas e visuais ficam na documentação técnica e no Design System.
   - Questione o fechamento de versão e mantenha `CHANGELOG.md`.
   - Submeta alterações exclusivamente via Pull Request formatado para a `master`.

👉 **Leia `AGENTS.md` e `.agents/rules/fluxo-desenvolvimento.md` agora.**
