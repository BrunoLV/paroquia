# Implementation Plan: Métricas Estratégicas e Preditivas de Gestão Paroquial

## Phase 1: Backend Domain, Schema & Liturgical Calendar Infrastructure
- [x] Task: Criar migração Flyway para tabela de Ciclos/Festas Litúrgicas (`tb_ciclo_liturgico`) e metadados de eventos/projetos 9675d8a
    - [x] Criar arquivo V...__create_tabela_ciclo_liturgico.sql com chave estrangeira e restrições
    - [x] Adicionar seed inicial com os principais períodos litúrgicos (Advento, Natal, Quaresma, Páscoa, Tempo Comum, Festas Padroeiras)
- [ ] Task: Implementar Entidades JPA e Repositórios com Clean Architecture
    - [ ] Criar entidade `CicloLiturgico` e enum `TipoCicloLiturgico`
    - [ ] Criar repositório Spring Data JPA `CicloLiturgicoRepository` e testes unitários/integração
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Backend Domain, Schema & Liturgical Calendar Infrastructure' (Protocol in workflow.md)

## Phase 2: Backend Analytics Engine & Metric Use Cases (TDD)
- [ ] Task: Implementar UseCase de Índice de Sazonalidade Litúrgica (Predictive Analytics)
    - [ ] Criar testes unitários TDD em `CalcularSazonalidadeLiturgicaUseCaseTest`
    - [ ] Implementar `CalcularSazonalidadeLiturgicaUseCase` cruzando histórico de eventos com calendário litúrgico
- [ ] Task: Implementar UseCase de Taxa de Conclusão e Abandono de Projetos
    - [ ] Criar testes unitários TDD em `CalcularTaxaProjetosUseCaseTest`
    - [ ] Implementar `CalcularTaxaProjetosUseCase` calculando percentual concluído/cancelado/em andamento
- [ ] Task: Implementar UseCase de SLA de Despacho Pastoral
    - [ ] Criar testes unitários TDD em `CalcularSlaDespachoPastoralUseCaseTest`
    - [ ] Implementar `CalcularSlaDespachoPastoralUseCase` medindo tempo médio de aprovação
- [ ] Task: Implementar UseCase de Coeficiente de Impacto Social (Pastoral em Saída)
    - [ ] Criar testes unitários TDD em `CalcularImpactoSocialUseCaseTest`
    - [ ] Implementar `CalcularImpactoSocialUseCase` agregando eventos sociais vs institucionais
- [ ] Task: Implementar Motor e UseCase de Alerta de Concorrência Interpastoral (Overlap Conflict)
    - [ ] Criar testes unitários TDD em `VerificarConcorrenciaInterpastoralUseCaseTest`
    - [ ] Implementar `VerificarConcorrenciaInterpastoralUseCase` e endpoint de validação preventiva
- [ ] Task: Implementar UseCase de Índice de Ociosidade Patrimonial
    - [ ] Criar testes unitários TDD em `CalcularOciosidadePatrimonialUseCaseTest`
    - [ ] Implementar `CalcularOciosidadePatrimonialUseCase` calculando horas vagas de espaços paroquiais
- [ ] Task: Expor Controller REST `MetricaEstrategicaController` com OpenAPI/Swagger
    - [ ] Criar endpoints REST para consulta das 6 métricas com DTOs anonimizados (LGPD)
    - [ ] Criar testes de integração `@SpringBootTest` com H2 cobrindo todos os endpoints
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Backend Analytics Engine & Metric Use Cases (TDD)' (Protocol in workflow.md)

## Phase 3: Frontend API Services & Real-time Conflict Warning Modal (TDD)
- [ ] Task: Criar Serviços API e Custom Hooks em React Query
    - [ ] Implementar `metricasService.ts` e hook `useMetricasEstrategicas` em `gestao-paroquia-ui`
    - [ ] Escrever testes unitários em Vitest para `metricasService`
- [ ] Task: Implementar Alerta Preventivo de Concorrência Interpastoral nos Modais
    - [ ] Integrar verificação de sobreposição no modal de cadastro/homologação de eventos
    - [ ] Adicionar componente de alerta visual (MUI Alert / Banner) informando potencial conflito antes de salvar
    - [ ] Escrever testes unitários em Vitest para o componente de alerta
- [ ] Task: Conductor - User Manual Verification 'Phase 3: Frontend API Services & Real-time Conflict Warning Modal (TDD)' (Protocol in workflow.md)

## Phase 4: Frontend Strategic Metrics Dashboard & Executive Reports UI
- [ ] Task: Implementar Aba "Métricas Estratégicas" na Tela de Relatórios Gerenciais
    - [ ] Criar componente `MetricasEstrategicasTab.tsx` com gráficos interativos (`@mui/x-charts`)
    - [ ] Renderizar Sazonalidade Litúrgica, Taxa de Projetos, SLA de Despacho, Impacto Social, Concorrência e Ociosidade Patrimonial
- [ ] Task: Implementar Widgets de Destaque Executivo no Dashboard Principal
    - [ ] Criar widgets de resumo executivo em `DashboardPage.tsx` para Clero e Conselhos (CPP/CAE)
- [ ] Task: Testes de Integração e Verificação End-to-End da UI
    - [ ] Executar suíte de testes Vitest (`npm test`) garantindo zero regressão e >80% coverage
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Frontend Strategic Metrics Dashboard & Executive Reports UI' (Protocol in workflow.md)
