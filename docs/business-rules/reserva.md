# Regras de Negócio e Fluxos: Módulo Reserva de Espaços (`reserva`)

O módulo de Reserva gerencia solicitações de uso temporário, locações comunitárias e bloqueios paroquiais de salas, salões e quadras para fins pastorais e sociais.

---

## 1. Visão Geral e Propósito
Permitir a reserva estruturada de dependências físicas da paróquia para retiros, casamentos, aniversários comunitários, velórios ou reuniões de entidades parceiras, com controle financeiro (taxas de limpeza/manutenção) e termos de uso.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`ReservaEspaco` (Raiz de Agregado):** Identificada por `ReservaEspacoId`. Contém espaço solicitado (`LocalId`), solicitante (fiel ou terceiro), período, finalidade, valor da contribuição/taxa, comprovante de pagamento, status da reserva e termo de compromisso assinado.
- **`StatusReserva` (Enum):** `SOLICITADA`, `PRE_RESERVADA`, `CONFIRMADA`, `CANCELADA`, `RECUSADA`.
- **`TermoCompromisso` (Value Object):** Regras de silêncio, horário limite, proibição de bebidas alcoólicas sem autorização e responsabilidade por danos.

---

## 3. Regras de Negócio Invariantes

### RN-RES-01: Precedência Litúrgica e Pastoral
- Atividades litúrgicas oficiais (missas, celebrações de sacramentos) e eventos pastorais aprovados têm **precedência absoluta** sobre qualquer reserva social particular.
- Caso haja necessidade emergencial de uso do espaço para a paróquia, a reserva particular pode ser cancelada ou remanejada com antecedência mínima regulamentar e estorno de taxa.

### RN-RES-02: Choque de Reserva com Calendário
- Nenhuma reserva pode ser confirmada se houver evento agendado ou outra reserva ativa para o mesmo espaço e horário.

### RN-RES-03: Homologação Administrativa
- Reservas de salão paroquial exigem análise de viabilidade pela secretaria e validação do CAEP ou Pároco antes da confirmação final.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Solicitação de Reserva:** Entrada de pedido via balcão da secretaria ou portal.
2. **Análise de Conflitos:** Validação automática com o módulo `local` e `calendario`.
3. **Confirmação e Emissão de Contrato:** Registro do pagamento de taxa e termo de compromisso.

---

## 5. Integrações com outros Bounded Contexts
- **`local`:** Bloqueio de datas no espaço físico.
- **`calendario`:** Exibição da reserva como ocupação de espaço na grade para evitar sobreposições.
- **`fiel`:** Identificação do solicitante caso seja paroquiano registrado.
