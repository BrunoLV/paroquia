# Track Spec: Métricas Estratégicas e Preditivas de Gestão Paroquial

## 1. Visão Geral
Expandir a inteligência gerencial da paróquia com 6 métricas estratégicas preditivas e analíticas voltadas ao Clero e aos Conselhos (CPP/CAE), respeitando estritamente a LGPD e o Spec-Driven Design (SDD).

## 2. Métricas Estratégicas
1. **Índice de Sazonalidade Litúrgica (Predictive Pastoral Analytics):** Cruzamento do histórico de eventos com tabela de ciclos/festas litúrgicas da Igreja para prever picos de demanda operacionais.
2. **Taxa de Conclusão e Abandono de Projetos (Project Completion Rate):** Proporção estatística de projetos concluídos vs. cancelados/abandonados (ciclo de vida da entidade Projeto).
3. **SLA de Despacho Pastoral:** Tempo médio entre criação da solicitação de evento pela pastoral e homologação final pelo Pároco/Conselhos.
4. **Coeficiente de Impacto Social (Pastoral em Saída):** Proporção entre eventos sociais/caridade vs. reuniões internas institucionais.
5. **Alerta de Concorrência Interpastoral (Overlap Conflict):** Identificação de sobreposição de públicos/temas em datas próximas por pastorais distintas (alerta em tempo real na homologação + widget analítico).
6. **Índice de Ociosidade Patrimonial:** Cálculo de horas vagas dos espaços físicos (`Local`) em horário útil durante a semana.

## 3. Diretrizes LGPD & Segurança
- Exclusivamente metadados temporais, quantitativos e categorizações/tags.
- Nenhuma exposição ou processamento de PII (dados nominais de fiéis ou voluntários).
- Identidades nas trilhas de auditoria mantidas criptografadas no backend.

## 4. Arquitetura & Interface
- **Backend (gestao-paroquia):** Endpoints REST em `MetricaEstrategicaController` / UseCases com suporte a Flyway migrations para tabela de Ciclos Litúrgicos.
- **Frontend (gestao-paroquia-ui):** Nova aba "Métricas Estratégicas" em Relatórios, Cards de Destaque no Dashboard principal e Alerta Preventivo de Concorrência no modal de agendamento/homologação.

## 5. Critérios de Aceite
- >80% de cobertura de testes unitários e integração no backend e frontend.
- Migrações Flyway e Seed para Tabela Litúrgica.
- Validação no motor de agendamento/homologação para detecção de concorrência.
