# Specification: Motor de Validação da Agenda Comunitária (Níveis de Prioridade 1-4 & Bypass)

## Overview
Implementação do motor de resolução de conflitos e validação da agenda comunitária por Níveis de Prioridade Eclesial (Nível 1 - Solenidade Global, Nível 2 - Litúrgico Territorial, Nível 3 - Ação Orgânica Regular, Nível 4 - Atividades de Movimentos), com tratamento de exceção `400 Bad Request: Conflito de Agenda Eclesial` e flag de bypass exclusivo para o Pároco (`bypass_agenda_conflict`).

## Functional Requirements
1. **Mapeamento de Prioridades:** Associar as categorias de evento a Níveis de Prioridade (1 a 4).
2. **Serviço de Validação de Prioridade (`AgendaPriorityValidatorService`):**
   - **Nível 1 (Solenidade Global):** Ao salvar evento Nível 1, varrer o banco e cancelar/bloquear automaticamente todas as atividades conflitantes em todas as capelas na mesma data/horário.
   - **Nível 2 (Litúrgico Territorial):** Impedir que reuniões de pastorais ou movimentos ocupem o mesmo espaço físico nos horários de missa da comunidade.
   - **Nível 4 (Movimentos):** Rejeitar o agendamento se houver sobreposição com Nível 1, 2 ou 3.
3. **Exceção HTTP 400:** Rejeitar conflitos de prioridade lançando a resposta HTTP `400 Bad Request: Conflito de Agenda Eclesial`.
4. **Bypass do Pároco:** Suportar o parâmetro `"bypass_agenda_conflict": true` nas requisições, liberando o salvamento apenas se o token JWT contiver a credencial `PAROCO`.

## Acceptance Criteria
- [ ] Criar um evento Nível 1 automaticamente cancela/bloqueia eventos de menor prioridade agendados para o mesmo período.
- [ ] Tentativas de agendamento que violem prioridades retornam exatamente `400 Bad Request: Conflito de Agenda Eclesial`.
- [ ] Usuários com role `PAROCO` conseguem forçar o agendamento enviando `"bypass_agenda_conflict": true`.
