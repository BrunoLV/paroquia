# Specification: Modularização do Calendário

## 1. Overview
O módulo `calendario` cresceu e acumulou responsabilidades além de sua função principal (gestão de eventos e agenda). Esta especificação detalha a extração sequencial de quatro conceitos distintos para seus próprios módulos de primeiro nível, visando melhorar a coesão, facilitar a manutenção e promover a reutilização de componentes transversais.

## 2. Functional Requirements

### 2.1 Módulo `observabilidade`
*   **Escopo:** Componentes transversais de auditoria e controle técnico.
*   **Componentes a extrair:** `AuditoriaOperacaoEntity`, `JobLockEntity`, `AuditLogPersistenceService`.
*   **Objetivo:** Permitir que módulos como `iam`, `local` e `organizacao` possam auditar operações sem dependerem do módulo `calendario`.

### 2.2 Módulo `governanca`
*   **Escopo:** Regras de negócio de alto nível que governam a operação da paróquia.
*   **Componentes a extrair:** `AnoParoquialEntity`, controllers, repositórios e serviços de autorização associados ao Ano Paroquial.
*   **Objetivo:** Separar as regras que definem o estado do calendário (aberto/fechado) do conteúdo do calendário (os eventos).

### 2.3 Módulo `projeto`
*   **Escopo:** Agrupamento e métricas de eventos relacionados a um objetivo comum.
*   **Componentes a extrair:** `ProjetoEventoEntity`, `ProjetoAgregacaoService`, DTOs associados e Controllers de Projeto.
*   **Objetivo:** Isolar a lógica de agregação de métricas ("Saúde Temporal") e colaboração entre organizações, que tende a evoluir com necessidades próprias de gestão de projetos.

### 2.4 Módulo `aprovacao`
*   **Escopo:** Engine de workflow e decisão de requisições.
*   **Componentes a extrair:** `AprovacaoEntity`, serviços de submissão e decisão (`DecideSolicitacaoAprovacaoUseCase`), listeners e payloads base.
*   **Restrições:** As lógicas específicas de aprovação de eventos (`CreateEventoExecutionStrategy`, `UpdateEventoExecutionStrategy`, etc.) devem permanecer no módulo de eventos (como implementações de uma interface do motor de aprovação).

## 3. Non-Functional Requirements
*   **Zero Regressões:** O comportamento externo da API REST não deve ser alterado (endpoints podem mudar de pacote Java, mas as URIs devem ser mantidas).
*   **Testes:** Todos os testes unitários e de integração existentes devem continuar passando após a reestruturação dos pacotes.
*   **Ordem de Execução:** A refatoração deve ser estritamente sequencial conforme definido no `plan.md` para mitigar conflitos de merge e problemas de dependência circular.

## 4. Acceptance Criteria
*   [ ] O pacote `br.com.nsfatima.gestao.calendario` contém apenas lógicas estritas a `Evento`.
*   [ ] Novos pacotes raiz criados: `observabilidade`, `governanca`, `projeto`, `aprovacao`.
*   [ ] Cobertura de código inalterada e testes rodando com sucesso.
