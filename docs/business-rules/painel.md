# Regras de Negócio e Fluxos: Módulo Painel / Dashboard (`painel` / `dashboard`)

O módulo de Painel consolida a visão executiva e operacional da liderança paroquial, integrando indicadores estratégicos, esteira de aprovações, gestão de risco de projetos e agenda imediata.

---

## 1. Visão Geral e Propósito
Oferecer ao Pároco, coordenadores e secretários uma visão unificada e em tempo real da vida paroquial, com alta densidade informacional, ergonomia visual e aderência estrita ao Design System.

---

## 2. Agregados, Entidades e Componentes Principais
- **`ResumoGeralDTO` (leitura):** métricas consolidadas: eventos com início no período, projetos ativos (`ABERTO` ou `EM_ANDAMENTO`), solicitações pendentes (eventos e projetos), total de fiéis ativos (`totalFieisAtivos`), novos cadastrados no mês corrente (`novosFieisMes`) e o engajamento pastoral (fiéis com e sem vínculo a pastoral ou movimento).
- **`AprovacoesPendentes` (Quadro Operacional):** Agrupamento sintético de contagens por tipo de solicitação, com direcionamento rápido para a esteira completa.
- **`ProjetosPorStatusCard` (Quadro Estratégico):** Visão descritiva das faixas de status (Planejamento, que reúne os projetos `ABERTO`, Em Execução, Concluídos e Atrasados), barras proporcionais e alerta de atrasos.
- **`ListaProgressoProjetosAtivos` (Quadro de Risco e Execução):** Listagem de projetos ativos ordenando prioritariamente no topo os projetos em risco (`emRisco === true`), exibindo chip pulsante e diagnóstico de risco.
- **`WeeklyTimeline` & `UpcomingEvents`:** Cronograma de atividades dos próximos 7 dias e celebrações iminentes.

---

## 3. Regras de Negócio Invariantes

### RN-PAI-01: Métrica Executiva de Fiéis Ativos
- O card de fiéis no painel exibe o **Total de Fiéis Ativos** (`resumo?.totalFieisAtivos`), utilizando o ícone corporativo `PersonOutlined`, cor temática `info.main` e legenda explicativa com o quantitativo de novos fiéis cadastrados no mês corrente (`+N novos no mês`).

### RN-PAI-02: Apresentação Sintética de Aprovações Pendentes
- O quadro de Aprovações Pendentes opera exclusivamente em modo sintético agregador, sem detalhamento individual de itens na tela principal.
- Exibe a contagem consolidada por tipo de solicitação através de ícones semânticos (`StatusIcon`), contadores (`Chip`) e botão de ação em largura total (*full-width*) direcionando para a esteira completa em `/aprovacoes`.

### RN-PAI-03: Priorização Obrigatória de Projetos em Risco
- A listagem de projetos ativos posiciona **obrigatoriamente no topo os projetos classificados com `emRisco === true`**.
- Projetos sob risco recebem identificação visual destacada (chip pulsante de erro, borda semântica e diagnóstico contextual explicando a causa do risco).

### RN-PAI-04: Composição de Grade e Foco Decisório
- O layout do painel organiza-se em grade simétrica de 12 colunas, reservada exclusivamente aos quadros de decisão operacional e estratégica (Aprovações, Linha do Tempo Semanal, Eventos Iminentes, Status de Projetos, Lista de Execução e Distribuição de Eventos).

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Entrada do Usuário:** Carregamento paralelo das métricas consolidadas via TanStack Query.
2. **Tomada de Decisão:** O Pároco verifica contagem de aprovações pendentes e projetos com descompasso de cronograma com ação em 1 clique.
3. **Navegação Semântica:** Acesso direto aos módulos de calendário, projetos e aprovações.

---

## 5. Integrações com outros Bounded Contexts
- **`fiel`:** Fornecimento do total de fiéis ativos e novos fiéis do mês.
- **`aprovacao`:** Quantitativos agregados por tipo de solicitação pendente.
- **`projeto`:** Métricas consolidadas por status e diagnósticos preditivos de risco.
- **`calendario`:** Eventos confirmados da semana e celebrações iminentes.
