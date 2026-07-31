# Implementation Plan - Identificação Visual do Tempo Litúrgico no Calendário

## Phase 1: Helper Litúrgico e Mapeamento Visual
- [ ] Task: Criar testes unitários e módulo utilitário para cálculo e estilo dos Tempos Litúrgicos (`liturgicalCalendar.ts`)
    - [ ] Write failing unit tests in `src/utils/liturgicalCalendar.test.ts` for liturgical season calculation and color mappings
    - [ ] Implement `src/utils/liturgicalCalendar.ts` returning season labels and subtle background colors
    - [ ] Confirm all tests pass and meet coverage requirements (>80%)
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Helper Litúrgico e Mapeamento Visual' (Protocol in workflow.md)

## Phase 2: Integração Visual no Grid de Dias (`Calendar.tsx`)
- [ ] Task: Integrar fundo sutil litúrgico e rótulo no grid de dias mantendo cartões de eventos inalterados
    - [ ] Update `src/pages/Calendar.test.tsx` to assert liturgical background colors and desktop text labels
    - [ ] Update `src/pages/Calendar.tsx` to apply subtle background colors on calendar day cells and render responsive labels
    - [ ] Confirm visual hierarchy (event cards retain original colors) and all unit tests pass
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Integração Visual no Grid de Dias (Calendar.tsx)' (Protocol in workflow.md)
