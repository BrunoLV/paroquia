# Regras de Negócio e Fluxos: Módulo Projetos Pastorais (`projeto`)

O módulo de Projetos gerencia iniciativas paroquiais com período definido (construções, reformas, missões, festas e campanhas), seu ciclo de vida e o vínculo de eventos do calendário ao cronograma do projeto, com indicador de risco de atraso.

---

## 1. Visão Geral e Propósito
Possibilitar o planejamento e o acompanhamento de projetos de médio e longo prazo, agrupando os eventos que os executam e sinalizando de forma antecipada os projetos em descompasso com o cronograma.

---

## 2. Agregados, Entidades e Value Objects Principais
- **`Projeto` (Raiz de Agregado):** identificado por UUID. Contém nome, descrição, organização responsável, início e fim e o status. A organização responsável pode ser transferida e o período redefinido.
- **`ProjetoStatus` (Enum):** `ABERTO`, `EM_ANDAMENTO`, `CONCLUIDO` e `CANCELADO`. `CONCLUIDO` e `CANCELADO` são estados terminais.
- **`ProjetoVinculo` (projeção):** o que a política de vínculo precisa saber do projeto (status, nome e período) ao receber um evento.
- **Saúde temporal:** o percentual de tempo decorrido do projeto e o indicador `emRisco`, calculados na leitura e apresentados no painel e na lista de projetos.

---

## 3. Regras de Negócio Invariantes

### RN-PROJ-01: Ciclo de Vida
- A transição é `ABERTO → EM_ANDAMENTO → CONCLUIDO`. O cancelamento só ocorre a partir de `ABERTO` ou `EM_ANDAMENTO`. Projeto concluído ou cancelado não sofre nova transição.
- Iniciar só vale para projeto `ABERTO`, e concluir só para `EM_ANDAMENTO`.
- O início pode ser **automático**: uma rotina diária leva a `EM_ANDAMENTO` os projetos `ABERTO` cuja data de início chegou.
- O nome é obrigatório e o fim do projeto não pode ser anterior ao início.

### RN-PROJ-02: Criação e Ano Paroquial
- Se o ano de início do projeto pertence a um **ano paroquial fechado ou concluído**, a criação direta é bloqueada e gera uma **solicitação de aprovação** pendente (ver `aprovacao.md`). Com o ano aberto, o projeto é criado já como `ABERTO`.

### RN-PROJ-03: Vínculo de Eventos
- Um evento só se vincula a projeto que não seja terminal (`CONCLUIDO` ou `CANCELADO`) e cujo período contenha o do evento.
- O evento recorrente vinculado a projeto precisa ter **limite** (data limite ou número de ocorrências), e todas as ocorrências ficam dentro do período do projeto.

### RN-PROJ-04: Indicador de Risco
- O projeto é calculado como **em risco** (`emRisco`) quando há ao menos um **evento pendente** (evento do projeto que ainda não terminou) e o **tempo decorrido ultrapassa 80%** do período planejado. Sem eventos pendentes, não há risco.
- Para o painel, o projeto ativo também é contado como **atrasado** quando o fim planejado já passou.

### RN-PROJ-05: Ordenação Prioritária no Painel
- No painel, os projetos em risco são exibidos **no topo da lista**, com sinalização visual de alerta.

### RN-PROJ-06: Alçada de Autorização de Projetos (`PadraoAutorizacaoProjetoPolicy`)
- **`PAROCO` e `BISPO_DIOCESANO`:** alçada global; criam, editam, iniciam e concluem projetos de qualquer organização.
- **`VIGARIO` e `SECRETARIA_PAROQUIAL`:** podem criar e editar projetos, mas não iniciar nem concluir; quando atuam por uma organização, só nos projetos dela.
- **`COORDENADOR`:** cria e edita projetos da própria organização e também os inicia e conclui.
- **`SECRETARIO_GRUPO` e `SECRETARIO_CONSELHO`:** criam e editam projetos da própria organização, sem iniciar nem concluir.
- **`ADMIN`:** isolado na infraestrutura técnica; não tem alçada sobre projetos pastorais.

- Redefinir o período do projeto só é aceito se nenhum evento não cancelado vinculado ficar fora dele (início antes do novo início ou fim depois do novo fim).

### RN-PROJ-07: Transferência de Responsabilidade
- Transferir o projeto a outra organização exige alçada de edição sobre a organização atual **e** sobre a de destino; o destino deve existir e estar ativa.

### RN-PROJ-08: Ativação Automática e Fuso
- A rotina diária (00:00:01 no fuso da paróquia) inicia os projetos `ABERTO` cujo início chegou, sempre pela transição do próprio projeto (`iniciar`). O ano paroquial que condiciona a criação é o do dia civil de Brasília do início.

---

## 4. Fluxos Operacionais e Casos de Uso
1. **Criação:** o responsável cadastra o projeto com nome, descrição, organização e período; em ano fechado, a criação segue para aprovação.
2. **Execução:** o projeto é iniciado (manual ou automaticamente), recebe eventos vinculados e é concluído ou cancelado.
3. **Visão de Risco:** o painel mostra a distribuição por status, os projetos atrasados e os em risco.

---

## 5. Integrações com outros Bounded Contexts
- **`aprovacao`:** solicitação de criação de projeto em ano paroquial fechado.
- **`calendario`:** eventos vinculados ao projeto, que definem o que ainda está pendente.
- **`governanca`:** o ano paroquial que condiciona a criação.
- **`painel`:** métricas e indicadores de risco.
