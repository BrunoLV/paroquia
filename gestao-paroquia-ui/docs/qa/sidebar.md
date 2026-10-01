# Menu lateral — organização por finalidade

Data: 2026-10-01. Branch: `fix/alinhar-visual-autocomplete`.

Fixture com `Layout`, navegação, fonte e tema de produção. Sessão sintética de Pároco; sem dados reais ou consultas à API. Chromium headless, zoom 100%.

- [Claro — seis seções](sidebar-light.png), [escuro](sidebar-dark.png): 1366×1100 para inspecionar toda a hierarquia.
- [Compacto](sidebar-compact.png): 1366×768, ícones separados por grupo e rota ativa destacada.
- [Mobile](sidebar-mobile.png): 390×844, gaveta aberta, largura 240 px e rolagem interna dos links.
- Cabeçalhos em caixa normal, fundo sutil, recuo dos links e divisor vertical discreto.
- Configuração compartilhada conserva rotas e predicados de autorização; nenhum grupo vazio é exibido.
- Testes cobrem ordem dos 18 itens, paridade dos 14 papéis, recolhimento por teclado, abertura da seção ao mudar rota, nomes acessíveis, modo compacto e fechamento mobile após navegação.

Imagens de fixture isolada, não de sessão autenticada nas telas de negócio.

Validação: 570 testes aprovados em 90 arquivos (`--maxWorkers=2 --minWorkers=1`); build de produção aprovado. Grafo estrutural atualizado. Avisos existentes de React Router/Zod e quatro fontes JSON sem nós no grafo permanecem fora do escopo desta mudança visual.
