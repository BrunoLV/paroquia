# Paginação no rodapé da tabela

- [Tema claro](pagination-footer-light.png)
- [Tema escuro](pagination-footer-dark.png)
- [Viewport reduzido](pagination-footer-mobile.png)

Capturas de fixture isolada em Chrome headless, usando `SearchableTablePagination`, `SearchableSelect`, tema e fonte de produção. Dados sintéticos e sem APIs; não representam sessão autenticada. Viewports de 1440×900 e 390×900.

As medições no navegador, nos três cenários, confirmam:

- `tfoot` dentro de `table`, com `td colSpan=3` cobrindo as colunas da fixture.
- Seletor Linhas por página e botões anterior/próximo com 38 px de altura.
- TextField e autocomplete fora da paginação mantêm 54,125 px, sem receber a compactação do rodapé.
- Rótulo externo ligado ao input por `aria-labelledby`.

A suíte `tablePaginationArchitecture.test.ts` verifica as 21 paginações de produção em 17 arquivos; nenhuma fica fora da respectiva tabela. `SearchableTablePagination.test.tsx` cobre pesquisa, retorno à primeira página, navegação e estrutura semântica. As capturas da paginação anteriores a este ajuste são substituídas como referência por estas evidências.
