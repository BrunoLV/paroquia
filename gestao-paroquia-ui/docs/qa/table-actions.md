# Ações de tabela — validação visual

Data: 2026-10-01.

Fixture isolada com `TableActionMenu`, tema e fonte de produção, dados sintéticos e sem chamadas à API. Chromium headless, zoom 100%, desktop 1366×768 e viewport estreito 390×844.

- [Tema claro](table-actions-light.png), [tema escuro](table-actions-dark.png) e [viewport estreito](table-actions-mobile.png).
- Gatilho delineado, compacto, somente `⋮` vertical; sem texto visível. Coluna mantém cabeçalho “Ações”.
- Paginação permanece no rodapé; campos de formulário mantêm a densidade original.
- Testes do menu verificam tooltip, nome acessível, abertura por teclado, foco restaurado, ações desabilitadas e ausência de propagação para a linha.
- Teste estrutural impede botões diretos em células; testes de reservas preservam opções por estado e permissão de aprovação.

Esta evidência usa componentes reais em fixture, não uma sessão autenticada das páginas.

Validação automatizada: suíte completa aprovada; skill frontend validada pelo validador de skills. Os testes novos de permissões complementam os testes existentes da tabela de reservas, preservados sem alteração.
