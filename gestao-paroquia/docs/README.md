# 📚 Portal de Documentação - Gestão Paróquia

O **Gestão Paróquia** é um sistema monolítico modular desenvolvido para a Paróquia Nossa Senhora de Fátima, fornecendo uma plataforma robusta, segura e escalável para o gerenciamento administrativo, pastoral e de fiéis.

---

## 🎯 Visão do Sistema

O sistema centraliza todas as atividades paroquiais em uma arquitetura limpa, integrando desde o agendamento de eventos e controle de espaços até a gestão de membros, movimentos pastorais, relatórios executivos e auditoria de segurança.

---

## 🏛️ Contextos Delimitados (Bounded Contexts)

A aplicação é dividida em 12 domínios (`br.com.nsfatima.gestao`):

1. **Gestão de Eventos e Calendário (`calendario`)**: Ciclo de vida completo de eventos paroquiais, aprovações, solicitações e regras de recorrência.
2. **Workflows de Aprovação (`aprovacao`)**: Fluxos de decisão, pareceres e aprovações hierárquicas de solicitações.
3. **Gestão de Locais e Espaços (`local`)**: Controle de salas, auditórios, capacidades e atributos de infraestrutura.
4. **Identidade e Acesso (`iam`)**: Gestão avançada de usuários, papéis (Roles), permissões e autenticação JWT.
5. **Auditoria e Governança (`governanca`)**: Rastreabilidade de ações, políticas de segurança e logs de auditoria.
6. **Estrutura Organizacional (`organizacao`)**: Hierarquia paroquial, matriz de setores, pastorais, movimentos e conselhos.
7. **Gestão de Membros (`membro`)**: Fichas cadastrais de fiéis, históricos e vínculos organizacionais.
8. **Gestão de Projetos (`projeto`)**: Vinculação de eventos a projetos paroquiais com métricas de impacto e status.
9. **Importação e Exportação (`importacao`)**: Processamento de relatórios em Excel (Apache POI) e PDF (OpenPDF).
10. **Painel Executivo (`dashboard`)**: Indicadores gerais, consolidação estatística e gráficos de gestão.
11. **Observabilidade (`observabilidade`)**: Telemetria, MDC, logs estruturados em JSON e healthchecks.
12. **Suporte Transversal (`support`)**: Tratamento centralizado de exceções, utilitários e abstrações de infraestrutura.

---

## 🛠️ Fundação Técnica

Construído com **Java 25** (Gradle Toolchain) e **Spring Boot 3.x**, o sistema adota rigorosamente os princípios de **Domain-Driven Design (DDD)** e **Clean Architecture (Ports & Adapters / Hexagonal)** para garantir sustentabilidade, testabilidade e evolução a longo prazo.

- **Persistência:** PostgreSQL 16 com Flyway migrations (Produção) & H2 (Testes automatizados).
- **Qualidade de Código:** Quality Gates obrigatórios via Checkstyle 13.8.0, PMD 7.16.0, SpotBugs e JaCoCo.
- **Privacidade (LGPD):** Estrita conformidade com as regras de proteção de dados descritas em [`ia/regra_dura_lgpd.md`](ia/regra_dura_lgpd.md).

---

## 🔗 Navegação da Documentação

- [Guia de Início Rápido](getting-started.md)
- [Visão Geral da Arquitetura](architecture.md)
- [Documentação dos Domínios](domains/)
  - [Domínio de Aprovação](domains/aprovacao.md)
  - [Domínio de Calendário](domains/calendario.md)
  - [Domínio de Governança](domains/governanca.md)
  - [Domínio de IAM](domains/iam.md)
  - [Domínio de Locais](domains/local.md)
  - [Domínio de Membros](domains/membro.md)
  - [Domínio de Observabilidade](domains/observabilidade.md)
  - [Domínio de Organização](domains/organizacao.md)
  - [Domínio de Projetos](domains/projeto.md)
  - [Domínio de Suporte](domains/support.md)
- [Regras Duras de Proteção de Dados (LGPD)](ia/regra_dura_lgpd.md)

