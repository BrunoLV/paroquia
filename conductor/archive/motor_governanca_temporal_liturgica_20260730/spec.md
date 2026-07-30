# Specification: Motor de Governança Temporal e Sazonalidade Litúrgica

**Track ID:** `motor_governanca_temporal_liturgica_20260730`  
**Tipo:** Feature  
**Escopo:** `gestao-paroquia` (Backend) & `gestao-paroquia-ui` (Frontend)

---

## 1. Contexto do Problema e Regra de Desacoplamento Temporal

O núcleo do calendário paroquial opera sob três janelas conceituais concorrentes que devem ser integradas de forma desacoplada no banco de dados:

1. **Ano Paroquial (Administrativo/Fiscal):** Segue estritamente o calendário civil gregoriano (01/Jan a 31/Dez). Governa o status de `lock` do banco de dados para segurança de auditoria administrativa paroquial.
2. **Ano Litúrgico (Estrutura do Ano):** Flutua entre as fronteiras civis. Inicia no **Primeiro Domingo do Advento** (fim de novembro/início de dezembro do ano civil anterior) e termina no sábado anterior ao Advento do ano seguinte. Nomeado pelo ano civil em que ocorre a sua respectiva Páscoa (Anos A, B ou C).
3. **Ciclo Litúrgico (Sazonalidade/Tempos):** Mapeia os tempos e festas da Igreja Católica (`ADVENTO`, `NATAL`, `QUARESMA`, `PASCOA`, `FESTA_PADROEIRA`, `TEMPO_COMUM`). Possui cores canônicas e o `fatorDemanda` (coeficiente preditivo para planejar a carga operacional e alocação de recursos da paróquia). Cada ciclo litúrgico deve considerar o Ano Litúrgico para suas definições e abrangência.

---

## 2. Requisitos Funcionais (Backend & Banco de Dados)

### A. Modelagem Relacional & Migrações (`V050__create_tabela_anos_liturgicos.sql`)
- Criar a tabela `calendario.anos_liturgicos`:
  - `ano INT PRIMARY KEY` (Ano da Páscoa, ex: 2026, 2027)
  - `ciclo_letra CHAR(1) NOT NULL` ('A', 'B' ou 'C')
  - `data_inicio_advento TIMESTAMP WITH TIME ZONE NOT NULL`
  - `data_fim_comum TIMESTAMP WITH TIME ZONE NOT NULL`
  - `updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP`
- Atualizar a tabela de eventos (`calendario.eventos` / `tb_projeto_evento`) adicionando:
  - `ano_liturgico_ref INT` (FK para `calendario.anos_liturgicos(ano)`)
  - `ciclo_liturgico_id UUID` (FK para `calendario.tb_ciclo_liturgico(id)`)
- **Seed Inicial Mandatório (10 Anos Litúrgicos 2026–2035):**
  - Cadastrar pelo menos 10 Anos Litúrgicos (2026 a 2035) com suas respectivas letras canônicas e datas de início do Advento.
  - Cadastrar/atualizar a malha de Ciclos Litúrgicos (`tb_ciclo_liturgico`) associados explicitamente a cada um dos 10 Anos Litúrgicos (2026 a 2035).
  - Atualizar os scripts de Seed do Docker para incluir a massa de dados dos 10 anos litúrgicos.

### B. Camada de Domínio & Persistência (Spring Boot 3.x)
- Criar record/modelo `AnoLiturgico`, entidade JPA `AnoLiturgicoEntity` e repositório `AnoLiturgicoRepository`.

### C. Motor Litúrgico (`LiturgicalEngine.java`)
- Implementar algoritmo de Meeus/Jones/Butcher para cálculo dinâmico da Páscoa.
- Calcular dinamicamente o 1º Domingo do Advento e resolver o Ano Litúrgico espiritual e o Ciclo Litúrgico sazonal a partir de qualquer data UTC.

### D. Orquestração nos Use Cases de Evento
- Integrar `CalendarLockPolicy` para validar se o Ano Paroquial civil da data do evento está `FECHADO`. Se estiver fechado, rejeita a criação a menos que o status seja `ADICIONADO_EXTRA` ou enviado para homologação/aprovação.
- Preencher automaticamente `anoLiturgicoRef` e `cicloLiturgicoId` na criação e edição de eventos.

### E. Ingestão em Lote e Resolução Silenciosa de Conflitos (`LiturgicalImportService.java`)
- Durante a importação de CSV/XLSX em um Ano Paroquial `FECHADO`:
  - Se a linha possuir `status_evento = ADICIONADO_EXTRA`, salva o registro.
  - Caso contrário, intercepta o registro, altera o status para `PENDENTE` e encaminha diretamente para a fila de solicitação de aprovação (`ProjetoSolicitacaoAprovacaoEntity`), evitando falhas ou rollbacks do lote de importação.

---

## 3. Requisitos Funcionais (Frontend `gestao-paroquia-ui`)

1. **Bloqueio e Form de Inclusão Extra (`Calendar.tsx` & `EventRegistrationModal.tsx`):**
   - O frontend verifica o status do Ano Paroquial. Se `FECHADO`, oculta o atalho de inclusão rápida e habilita o formulário de "Solicitação de Inclusão Extra" exigindo justificativa textual.
2. **Indicadores Litúrgicos no Calendário:**
   - Exibir cores canônicas (Roxo, Verde, Branco, Vermelho, Rosa) e badge do `fatorDemanda` na grade do calendário.
3. **Widgets no Dashboard (`Dashboard.tsx`):**
   - Atualizar os widgets executivos consumindo os dados consolidados da governança temporal e sazonalidade.

---

## 4. Critérios de Aceite
- Todos os testes unitários e de integração no backend (`./gradlew test`) devem passar sem erros.
- A suíte de testes do frontend (`npm test`) deve passar com 0 regressões.
- A migração Flyway V050 e os scripts Docker Seed devem executar de forma idempotente e popular os 10 anos litúrgicos (2026-2035) com seus ciclos correspondentes.
