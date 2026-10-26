# Regras de Negócio e Fluxos: Módulo Reservas (`reserva`)

O módulo de Reserva gerencia o uso temporário de dependências físicas (locais) e de bens móveis do patrimônio por pastorais e movimentos, com aprovação, retirada e devolução.

---

## 1. Visão Geral e Propósito
Permitir a reserva estruturada de locais e de ativos patrimoniais, sempre em nome de uma pastoral ou movimento, evitando choque de agenda e registrando a custódia do bem (retirada e devolução com conferência de avarias).

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Reserva` (Raiz de Agregado):** identificada por `ReservaId`. Contém o tipo e o identificador do item reservado, o intervalo, a descrição da finalidade, o solicitante, a pastoral ou movimento em nome de quem se reserva, o evento e o grupo de recorrência de origem (quando houver), o status, o aprovador e a data de aprovação, o motivo e o autor do cancelamento, o relatório de avarias da devolução e a exclusão lógica.
- **`TipoItemReserva` (Enum):** `LOCAL` (dependência física) e `ATIVO` (bem móvel do patrimônio).
- **`StatusReserva` (Enum):** `PENDENTE`, `CONFIRMADA`, `RETIRADO`, `FINALIZADA`, `CANCELADA` e `EXPIRADA`. Ocupam a agenda todos os estados, exceto `CANCELADA` e `EXPIRADA`.
- **`IntervaloReserva` (Value Object):** início, fim e uma **margem logística** (`bufferMinutos`, 30 minutos por padrão, **no máximo 240**) para organização e limpeza. O fim é estritamente posterior ao início e a margem não é negativa.

---

## 3. Regras de Negócio Invariantes

### RN-RES-01: Reserva em Nome de Pastoral ou Movimento
- Toda reserva é feita em nome de uma **pastoral ou movimento**, e o solicitante precisa ter **vínculo ativo** com ela.

### RN-RES-02: Choque de Agenda
- Não se reserva um item cujo intervalo, **somada a margem logística** de cada reserva, colida com outra reserva ativa do mesmo item (o evento que usa um item o ocupa por uma reserva). Os turnos de um mesmo evento não colidem entre si. A verificação é feita sob bloqueio do item: dois pedidos simultâneos para o mesmo item são atendidos um de cada vez, e o segundo recebe o conflito (HTTP 409).
- Um **ativo em manutenção** (ou com manutenção prevista) no intervalo não pode ser reservado (ver `patrimonio.md`, RN-PAT-04). O bloqueio do item é o mesmo tomado ao enviar o ativo para manutenção, de modo que reserva e manutenção do mesmo bem também não se atropelam.
- Só se reserva item **existente, em uso e elegível a reserva**: local ativo e não excluído; ativo não excluído e marcado como reservável.
- Todo caminho de criação (pedido avulso, série recorrente e evento) aplica as mesmas verificações e notifica o patrimônio para refletir `RESERVADO` no bem.

### RN-RES-03: Status Inicial e Alçada
- A reserva nasce **`CONFIRMADA`** quando criada a partir de um evento oficial, ou quando o solicitante é o Pároco, o administrador, a **secretaria paroquial** ou o coordenador da pastoral ou movimento. Nos demais casos nasce **`PENDENTE`**, aguardando análise. O vínculo com evento ou série recorrente **nunca é informado pelo cliente**: só a integração com eventos e o endpoint recorrente o estabelecem, o que impede que um pedido comum se autoconfirme.
- Aprovam ou recusam reservas pendentes o Pároco, o administrador, a secretaria paroquial e o coordenador da respectiva pastoral ou movimento. A secretaria paroquial tem a alçada administrativa (confirma, aprova, cancela e reserva por qualquer pastoral), por decisão de manter o comportamento já em uso.
- A reserva de evento exige solicitante, operador e pastoral identificados; não se inventa identidade.
- A **recusa** exige motivo e leva a reserva a `CANCELADA`.

### RN-RES-04: Cancelamento
- Pode cancelar o Pároco, o administrador, o coordenador da pastoral ou movimento e o próprio solicitante, este só enquanto a reserva é futura. O motivo é obrigatório e, quando o Pároco ou o administrador cancelam, tem no mínimo **15 caracteres**.
- Só reservas `PENDENTE` e `CONFIRMADA` são canceladas. Reservas `RETIRADO` (bem em custódia, que só sai pela devolução), `CANCELADA`, `FINALIZADA` ou `EXPIRADA` não são. O cancelamento de um evento cancela em cascata apenas as reservas canceláveis dele.

### RN-RES-05: Expiração das Pendentes
- A reserva `PENDENTE` que não for analisada até **48 horas antes do início** é **expirada** automaticamente, o que libera a agenda e notifica o solicitante. O pedido feito com **menos de 48 horas de antecedência** não expira ao nascer: seu prazo de análise é o próprio início. Cada expiração é confirmada isoladamente e a notificação ocorre depois; a falha de uma não afeta as demais.

### RN-RES-06: Custódia do Ativo
- Só a reserva de **ativo** `CONFIRMADA` tem a **retirada** registrada (`RETIRADO`). A **devolução** só vale sobre `RETIRADO` e exige o **relatório de conferência de avarias**; ela leva a reserva a `FINALIZADA`.
- A reserva de **local** `CONFIRMADA` é finalizada diretamente, sem conferência, por ato manual (`PATCH /reservas/{id}/finalizar`) do solicitante, do coordenador da pastoral ou movimento ou da administração, **depois de iniciado o uso**. Não há finalização automática.

### RN-RES-07: Reservas Recorrentes
- Uma série recorrente tem ao menos uma ocorrência; cada ocorrência segue as mesmas regras de choque, vínculo e status inicial.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Solicitação:** o membro da pastoral ou movimento reserva um local ou ativo para um intervalo, com a finalidade.
2. **Análise:** o coordenador, o Pároco ou o administrador aprova ou recusa; sem análise até 48 horas antes, a reserva expira.
3. **Uso do Ativo:** retirada e devolução com conferência de avarias.
4. **Uso do Local:** finalização direta depois do uso, pela ação "Finalizar Uso" da tela de reservas.
5. **Consulta:** disponibilidade de um item e métricas de reservas.

---

## 5. Integrações com outros Bounded Contexts
- **`local`:** os locais reservados e a ocupação do espaço físico.
- **`patrimonio`:** os ativos reservados; o patrimônio reflete no bem o status `RESERVADO` e impede a manutenção ou a exclusão com reservas ativas (ver `patrimonio.md`).
- **`calendario`:** reservas criadas por eventos oficiais e a exibição da ocupação na agenda.
- **`organizacao` e `membro`:** a pastoral ou movimento em nome de quem se reserva e o vínculo do solicitante.
