# Regras de Negócio e Fluxos: Módulo Projetos Pastorais (`projeto`)

O módulo de Projetos gerencia iniciativas paroquiais estruturadas (construções, reformas, missões populares, festas de padroeiro, campanhas sociais e catequéticas) com acompanhamento de metas, progresso e diagnósticos preditivos de risco.

---

## 1. Visão Geral e Propósito
Possibilitar o planejamento e acompanhamento estratégico de projetos de médio e longo prazo, permitindo identificar descompassos de cronograma e projetos sob risco de forma antecipada.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Projeto` (Raiz de Agregado):** Identificado por `ProjetoId`. Contém título, objetivo, justificativa, datas de início e término planejado, orçamento previsto/executado, status (`StatusProjeto`), percentual de progresso (0-100%), lista de metas/etapas e coordenador responsável (`FielId`).
- **`StatusProjeto` (Enum):** `PLANEJAMENTO`, `EM_EXECUCAO`, `CONCLUIDO`, `CANCELADO`, `ATRASADO`.
- **`MetaProjeto`:** Etapa mensurável do projeto com peso, prazo e indicador de conclusão.
- **`DiagnosticoRisco` (Value Object):** Indicador booleano `emRisco`, severidade e justificativa contextual (ex.: tempo decorrido vs progresso executado, eventos travados aguardando aprovação ou prazo estourado).

---

## 3. Regras de Negócio Invariantes

### RN-PROJ-01: Classificação e Diagnóstico Preditivo de Risco
Um projeto é automaticamente classificado como **Em Risco** (`emRisco === true`) quando atender a pelo menos um dos seguintes critérios objetivos:
1. **Prazo Expirado:** A data atual ultrapassou a data final planejada e o status ainda é `EM_EXECUCAO` ou `PLANEJAMENTO`.
2. **Descompasso Crítico de Cronograma:** O tempo decorrido do projeto já atingiu mais de 75% do prazo total, mas o progresso executado reportado é inferior a 50%.
3. **Bloqueio de Aprovação:** Há eventos ou etapas essenciais travados na esteira de aprovação com data iminente de execução.

### RN-PROJ-02: Ordenação Prioritária no Painel (Dashboard)
- Na visualização do Painel executivo, todos os projetos em risco (`emRisco === true`) **devem obrigatoriamente ser ordenados no topo da lista**, acompanhados de badge de alerta visual, borda de destaque e box com o motivo explicativo do diagnóstico de risco.

### RN-PROJ-03: Conclusão Condicionada a Metas
- Um projeto só pode transitar para o status `CONCLUIDO` se todas as suas etapas e metas obrigatórias estiverem registradas como concluídas ou se houver justificativa formal de dispensa aprovada pelo Pároco.

### RN-PROJ-04: Alçada de Autorização de Projetos (`PadraoAutorizacaoProjetoPolicy`)
- **`PAROCO` e `VIGARIO`:** Possuem alçada global paroquial para criar, editar e transicionar o ciclo de vida de projetos de qualquer pastoral ou comunidade.
- **`COORDENADOR` e `SECRETARIO_GRUPO`:** Podem propor e gerir projetos estritamente vinculados à sua própria organização (`organizacaoResponsavelId` idêntico ao vínculo ativo).
- **`ADMIN`:** Isolado na infraestrutura técnica; não possui alçada sobre projetos pastorais.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Elaboração e Submissão:** Proposição do plano do projeto pelo `COORDENADOR` com parecer consultivo do Conselho (`MEMBRO_CONSELHO_PASTORAL` / `MEMBRO_CONSELHO_ECONOMICO`) e homologação pelo `PAROCO`.
2. **Acompanhamento de Progresso:** Atualização de percentuais de avanço pelas coordenações (`COORDENADOR`).
3. **Visão de Risco e Alertas:** Exibição sintética de distribuição por status e alertas visuais no painel paroquial.

---

## 5. Integrações com outros Bounded Contexts
- **`aprovacao`:** Submissão de novos projetos à esteira de decisão.
- **`calendario`:** Vinculação de eventos e mutirões ao cronograma do projeto.
- **`painel`:** Alimentação dos cards `ProjetosPorStatusCard` e `ListaProgressoProjetosAtivos`.
