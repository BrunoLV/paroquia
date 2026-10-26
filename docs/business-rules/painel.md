# Regras de Negócio e Fluxos: Módulo Painel / Dashboard (`painel` / `dashboard`)

O módulo de Painel consolida a visão executiva e operacional da liderança paroquial: indicadores, aprovações pendentes, risco de projetos, logística de ambientes, vida comunitária e aniversariantes. Não persiste métricas: tudo é calculado sob demanda.

---

## 1. Visão Geral e Propósito
Oferecer ao Pároco, coordenadores e secretários uma visão unificada e atual da vida paroquial, respeitando o escopo organizacional de cada ator.

---

## 2. Conceitos e Seções
- **Resumo geral:** eventos com início no período, projetos ativos (`ABERTO` ou `EM_ANDAMENTO`), solicitações pendentes (eventos e projetos), fiéis ativos, novos fiéis do mês corrente e engajamento pastoral (fiéis com e sem vínculo ativo a pastoral ou movimento).
- **Projetos:** quantidade por faixa (planejamento = `ABERTO`, em execução, concluídos) e atrasados; detalhe de cada projeto ativo com percentual de tempo e de execução, eventos aguardando aprovação, risco, próximo marco e organizações envolvidas.
- **Eventos:** distribuição por organização no período, eventos da semana corrente e eventos órfãos (sem projeto) com sugestão de projeto.
- **Logística:** choques de horário por local, ocupação de ambientes, eventos por organização responsável e pendências de homologação por tipo.
- **Comunidade:** pastorais, movimentos e serviços em situação de atenção (menos de 3 voluntários ativos ou sem coordenador).
- **Aniversariantes:** aniversariantes do período (`HOJE`, `SEMANA`, `MES`).

---

## 3. Regras de Negócio Invariantes

### RN-PAI-01: Escopo Organizacional
- Ator global (sem organização) vê a visão geral ou a organização que escolher no filtro; ator vinculado a uma organização só vê a própria e pedir outra é negado. O escopo vale para **todas** as métricas, inclusive fiéis e aprovações: com escopo, "fiéis" são os fiéis ativos com vínculo ativo na organização, e as aprovações são as de eventos e projetos da organização.

### RN-PAI-02: Eventos Cancelados
- Evento `CANCELADO` não entra em nenhuma métrica de volume, de semana, de órfãos nem de choque.

### RN-PAI-03: Aprovações Pendentes
- Contam as aprovações no status `PENDENTE` (eventos e solicitações de projeto). O indicador "aguardando aprovação" de cada projeto conta aprovações pendentes dos eventos do projeto, e não eventos em rascunho. As pendências de homologação por tipo obedecem ao mesmo escopo.

### RN-PAI-04: Engajamento e Fiéis
- Fiel só conta (ativo, novo, engajado) se estiver com cadastro ativo; o vínculo de um fiel inativado não infla o engajamento. "Sem vínculo pastoral" é o total de ativos menos os engajados. "Novos no mês" parte da meia-noite do dia 1º em Brasília.

### RN-PAI-05: Priorização de Projetos em Risco
- Os projetos ativos em risco (`emRisco`) vêm primeiro na listagem. O risco e o atraso seguem `projeto.md` (RN-PROJ-04).

### RN-PAI-06: Ocupação de Ambientes
- A ocupação deriva das **reservas de local** (fonte de verdade), nos status que ocupam a agenda (todos exceto cancelada e expirada), recortadas na janela (dia / próximos 7 dias) e com as sobreposições unidas antes da soma. Com escopo, contam só as reservas solicitadas pela organização do escopo (organização responsável pelo evento), sobre o conjunto de todos os ambientes ativos. A capacidade considera 14 horas operacionais por dia; a taxa é limitada a 100%. Os choques de horário continuam restritos aos ambientes da organização do escopo.

### RN-PAI-07: Fuso Horário
- Hoje, semana (segunda a domingo), mês e aniversários são sempre do dia civil de Brasília. Quem nasceu em 29/02 comemora em **28/02** nos anos não bissextos (e em 29/02 nos bissextos). O recorte `SEMANA` são os próximos 7 dias a partir de agora.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Entrada:** o ator abre o painel e as seções são carregadas de forma independente (métricas, logística, comunidade, aniversariantes).
2. **Decisão:** o Pároco acompanha aprovações pendentes e projetos em risco e segue para a esteira de aprovação ou para o projeto.
3. **Endpoints:** `/api/v1/painel/metricas`, `/painel/logistica`, `/painel/comunidade` e `/painel/aniversariantes`, todos com filtro opcional de período e organização.

---

## 5. Integrações com outros Bounded Contexts
- **`fiel` e `membro`:** fiéis ativos, novos e vínculos pastorais.
- **`aprovacao`:** aprovações pendentes por tipo.
- **`projeto`:** status, período, risco e organizações envolvidas.
- **`calendario`:** eventos do período, da semana, órfãos e choques.
- **`reserva` e `local`:** reservas de local e ambientes ativos.
