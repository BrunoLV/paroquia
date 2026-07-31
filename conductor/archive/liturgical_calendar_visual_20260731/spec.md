# Specification: Identificação Visual do Tempo Litúrgico no Calendário

## Overview
Esta melhoria introduz a identificação visual dinâmica dos Tempos Litúrgicos da Igreja Católica no calendário paroquial (`Calendar.tsx`). Cada célula correspondente a um dia do calendário receberá um fundo suave e sutil indicando o Tempo Litúrgico vigente (Advento, Natal, Quaresma, Tríduo Pascal, Páscoa e Tempo Comum), mantendo os cartões de eventos com suas cores originais por categoria e exibindo o nome do tempo litúrgico em modo desktop.

## Functional Requirements
- **Cálculo/Mapeamento Litúrgico por Data:** Determinar o Tempo Litúrgico correspondente a cada dia do ano (Advento, Natal, Quaresma, Tríduo Pascal, Páscoa, Tempo Comum) com base na data do dia e no calendário litúrgico oficial (algoritmo de datas móveis como Páscoa, Quarta de Cinzas, Pentecostes, Advento).
- **Estilização Suave do Dia (Background):**
  - Renderizar uma cor de fundo pastel/suave e discreta no dia correspondente:
    - **Advento / Quaresma:** Roxo sutil (`rgba(156, 39, 176, 0.08)`)
    - **Tríduo Pascal:** Vermelho sutil (`rgba(211, 47, 47, 0.10)`)
    - **Páscoa / Natal:** Amarelo/Dourado sutil (`rgba(255, 193, 7, 0.10)`)
    - **Tempo Comum:** Verde sutil (`rgba(46, 125, 50, 0.08)`)
- **Preservação dos Cartões de Eventos:** Os blocos de eventos dentro de cada dia mantêm integralmente suas cores originais por categoria (`LITURGICO`, `PASTORAL_SOCIAL`, `ADMINISTRATIVO`, etc.).
- **Rótulo do Tempo Litúrgico (Desktop):** Exibir o nome do tempo em texto discreto e legível no topo de cada célula de dia em telas desktop (`md` e superior). Ocultar o texto em resoluções mobile (`xs`/`sm`), mantendo a cor de fundo sutil.
- **Transparência e Suporte a Dark/Light Mode:** As tonalidades devem ser suaves tanto no modo claro quanto no modo escuro da aplicação.

## Non-Functional Requirements
- **Performance:** O cálculo litúrgico dos dias exibidos no grid do calendário é memoizado via `useMemo`.
- **Acessibilidade:** Manter legibilidade e contraste conforme as diretrizes WCAG 2.1 AA.

## Acceptance Criteria
- [ ] O fundo de cada célula de dia no grid do calendário exibe a cor sutil correspondente ao Tempo Litúrgico.
- [ ] O nome do tempo litúrgico (ex: "ADVENTO", "QUARESMA", "TEMPO COMUM", "PÁSCOA", "NATAL", "TRÍDUO PASCAL") é exibido no topo de cada dia em resoluções desktop.
- [ ] Em telas mobile, a tonalidade de fundo é mantida e o rótulo de texto é ocultado.
- [ ] As cores dos cartões de eventos por categoria permanecem inalteradas.
- [ ] Testes no `Calendar.test.tsx` cobrem o cálculo litúrgico e a renderização do fundo/rótulo dos dias.

## Out of Scope
- Alterações nos dados do backend ou adição de endpoints REST dedicados.
- Alteração nas regras de bloqueio do Ano Paroquial ou no cadastro de eventos.
