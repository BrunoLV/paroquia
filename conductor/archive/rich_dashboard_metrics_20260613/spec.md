# Specification: Rich Dashboard & Managerial Metrics

## Overview
Esta track tem como objetivo enriquecer a tela de Dashboard do sistema de gestão paroquial, transformando-a em uma central gerencial moderna e visualmente impactante. A nova interface apresentará gráficos interativos, indicadores de progresso e saúde temporal de projetos ativos, resumos estatísticos e uma linha do tempo semanal. As métricas serão fornecidas por um novo endpoint unificado no backend, respeitando o contexto de permissões e organizações do usuário logado.

---

## Requirements

### 1. Backend: API de Métricas do Dashboard
* **Endpoint:** `GET /api/v1/dashboard/metrics` (ou `/metricas`).
* **Regra de Acesso e Filtragem:**
  * Usuários com role `ADMIN` ou `ROLE_ADMIN` visualizam as métricas consolidadas de todas as organizações da paróquia.
  * Demais usuários têm suas métricas restritas/filtradas apenas para as organizações às quais pertencem (resolvido via `EventoActorContextResolver`).
* **Estrutura de Resposta (Payload JSON):**
  * `resumoGeral`:
    * `totalEventosMes`: Quantidade de eventos criados/agendados no mês corrente.
    * `projetosAtivos`: Contagem de projetos com status `ABERTO` ou `EM_ANDAMENTO`.
    * `solicitacoesPendentes`: Contagem de solicitações de aprovação pendentes de decisão (eventos ou projetos).
  * `eventosPorOrganizacao`:
    * Lista de objetos contendo `organizacaoNome` e `quantidadeEventos` no mês corrente.
  * `projetosAtivosDetalhes`:
    * Lista de projetos ativos contendo: nome, status, data de início/fim, percentual de tempo decorrido, total de eventos vinculados, eventos realizados, e se está classificado como `emRisco` (alerta temporal com pendências).
  * `eventosSemana`:
    * Lista de eventos agendados para a semana corrente (de segunda-feira a domingo), contendo título, data/hora de início, local e organização responsável.

### 2. Frontend: Interface Gerencial & Gráficos
* **Biblioteca de Gráficos:** Instalar e utilizar `@mui/x-charts` para exibição de componentes gráficos.
* **Layout do Dashboard (Grade Responsiva):**
  * **Seção de Boas-Vindas:** Mensagem personalizada com o nome do usuário e sua organização ativa.
  * **Grade de Cards de Resumo:** Cards contendo ícones dinâmicos do MUI, fundos com gradientes suaves, efeitos hover de escala e métricas numéricas claras.
  * **Gráfico de Rosca/Donut:** Exibição da distribuição dos eventos do mês por Pastoral/Organização (usando `@mui/x-charts` PieChart).
  * **Saúde dos Projetos Ativos:** Listagem de projetos com barras de progresso lineares (MUI LinearProgress) para indicar o tempo decorrido do projeto e o percentual de eventos realizados, com destaque visual (cor vermelha/alerta) para projetos sinalizados como `emRisco`.
  * **Linha do Tempo Semanal (Weekly Timeline):** Componente de linha do tempo exibindo cronologicamente as atividades da paróquia da semana.
  * **Ações Rápidas (Quick Actions):** Atalhos rápidos para agendar eventos e exportar relatórios.

---

## Non-Functional Requirements
* **Acessibilidade & Responsividade:** Layout fluido adaptado para tablets e desktops.
* **Aesthetics:** Uso de gradientes modernos, sombras suaves para elevação e micro-animações nas interações de hover dos cards.
* **Performance:** Carregamento assíncrono com animações de skeleton loader durante a requisição à API.

---

## Acceptance Criteria
* O novo endpoint `/api/v1/dashboard/metrics` compila e executa com sucesso.
* Usuários não-admin têm seus dados de métricas devidamente filtrados.
* A interface do Dashboard renderiza corretamente os gráficos do `@mui/x-charts` e todas as seções planejadas sem quebras de layout.
* Testes automatizados no backend (JUnit) e frontend (Jest) cobrindo os novos fluxos e componentes passam com 100% de sucesso.
