# Implementation Plan: Métricas Estratégicas e Preditivas de Gestão Paroquial

## Phase 1: Backend Domain, Schema & Liturgical Calendar Infrastructure
- [x] Task: Criar migração Flyway para tabela de Ciclos/Festas Litúrgicas (`tb_ciclo_liturgico`) e metadados de eventos/projetos 9675d8a
    - [x] Criar arquivo V...__create_tabela_ciclo_liturgico.sql com chave estrangeira e restrições
    - [x] Adicionar seed inicial com os principais períodos litúrgicos (Advento, Natal, Quaresma, Páscoa, Tempo Comum, Festas Padroeiras)
- [x] Task: Implementar Entidades JPA e Repositórios com Clean Architecture 7d4d65a
    - [x] Criar entidade `CicloLiturgico` e enum `TipoCicloLiturgico`
    - [x] Criar repositório Spring Data JPA `CicloLiturgicoRepository` e testes unitários/integração
- [x] Task: Conductor - User Manual Verification 'Phase 1: Backend Domain, Schema & Liturgical Calendar Infrastructure' (Protocol in workflow.md) 3f63f93

## Phase 2: Backend Analytics Engine & Metric Use Cases (TDD)
- [x] Task: Implementar UseCase de Índice de Sazonalidade Litúrgica (Predictive Analytics) 8aa6732
    - [x] Criar testes unitários TDD em `CalcularSazonalidadeLiturgicaUseCaseTest`
    - [x] Implementar `CalcularSazonalidadeLiturgicaUseCase` cruzando histórico de eventos com calendário litúrgico
- [x] Task: Implementar UseCase de Taxa de Conclusão e Abandono de Projetos 27d3470
    - [x] Criar testes unitários TDD em `CalcularTaxaProjetosUseCaseTest`
    - [x] Implementar `CalcularTaxaProjetosUseCase` calculando percentual concluído/cancelado/em andamento
- [x] Task: Implementar UseCase de SLA de Despacho Pastoral 54098ec
    - [x] Criar testes unitários TDD em `CalcularSlaDespachoPastoralUseCaseTest`
    - [x] Implementar `CalcularSlaDespachoPastoralUseCase` medindo tempo médio de aprovação
- [x] Task: Implementar UseCase de Coeficiente de Impacto Social (Pastoral em Saída) bc5f4c2
    - [x] Criar testes unitários TDD em `CalcularImpactoSocialUseCaseTest`
    - [x] Implementar `CalcularImpactoSocialUseCase` agregando eventos sociais vs institucionais
- [x] Task: Implementar Motor e UseCase de Alerta de Concorrência Interpastoral (Overlap Conflict) 6273bf5
    - [x] Criar testes unitários TDD em `VerificarConcorrenciaInterpastoralUseCaseTest`
    - [x] Implementar `VerificarConcorrenciaInterpastoralUseCase` e endpoint de validação preventiva
- [x] Task: Implementar UseCase de Índice de Ociosidade Patrimonial a8067ff
    - [x] Criar testes unitários TDD em `CalcularOciosidadePatrimonialUseCaseTest`
    - [x] Implementar `CalcularOciosidadePatrimonialUseCase` calculando horas vagas de espaços paroquiais
- [x] Task: Expor Controller REST `MetricaEstrategicaController` com OpenAPI/Swagger 8ae5b19
    - [x] Criar endpoints REST para consulta das 6 métricas com DTOs anonimizados (LGPD)
    - [x] Criar testes de integração `@SpringBootTest` com H2 cobrindo todos os endpoints
- [x] Task: Conductor - User Manual Verification 'Phase 2: Backend Analytics Engine & Metric Use Cases (TDD)' (Protocol in workflow.md) 29f3d40

## Phase 3: Frontend API Services & Real-time Conflict Warning Modal (TDD)
- [x] Task: Criar Serviços API e Custom Hooks em React Query 7edd240
    - [x] Implementar `metricasService.ts` e hook `useMetricasEstrategicas` em `gestao-paroquia-ui`
    - [x] Escrever testes unitários em Vitest para `metricasService`
- [x] Task: Implementar Alerta Preventivo de Concorrência Interpastoral nos Modais 8682df1
    - [x] Integrar verificação de sobreposição no modal de cadastro/homologação de eventos
    - [x] Adicionar componente de alerta visual (MUI Alert / Banner) informando potencial conflito antes de salvar
    - [x] Escrever testes unitários em Vitest para o componente de alerta
- [x] Task: Conductor - User Manual Verification 'Phase 3: Frontend API Services & Real-time Conflict Warning Modal (TDD)' (Protocol in workflow.md) 8a2ff36

## Phase 4: Frontend Strategic Metrics Dashboard & Executive Reports UI
- [x] Task: Implementar Aba "Métricas Estratégicas" na Tela de Relatórios Gerenciais 5ebfcc1
    - [x] Criar componente `MetricasEstrategicasTab.tsx` com gráficos interativos (`@mui/x-charts`)
    - [x] Renderizar Sazonalidade Litúrgica, Taxa de Projetos, SLA de Despacho, Impacto Social, Concorrência e Ociosidade Patrimonial
- [x] Task: Implementar Widgets de Destaque Executivo no Dashboard Principal a1a9951
    - [x] Criar widgets de resumo executivo em `DashboardPage.tsx` para Clero e Conselhos (CPP/CAE)
- [x] Task: Testes de Integração e Verificação End-to-End da UI 9c2d394
    - [x] Executar suíte de testes Vitest (`npm test`) garantindo zero regressão e >80% coverage
- [ ] Task: Conductor - User Manual Verification 'Phase 4: Frontend Strategic Metrics Dashboard & Executive Reports UI' (Protocol in workflow.md)
