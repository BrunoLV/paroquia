---
name: fluxo-desenvolvimento
description: "Organiza e governa o ciclo de vida completo de desenvolvimento de software no monorepo Paróquia: atua nos modos Plan e Execution, impondo GitHub Flow (branches semânticas saindo da master atualizada), atualização obrigatória de Living Documentation em docs/business-rules/, ciclo de versionamento semântico no CHANGELOG.md e build.gradle, Conventional Commits e submissão via Pull Request padronizado para a master. Mandatória e impossível de ignorar."
---

# Skill: Fluxo Canônico de Desenvolvimento de Software (Plan & Execution)

Esta skill estabelece o fluxo oficial, padronizado e inegociável de engenharia de software para o **Projeto Paróquia**. Ela rege **qualquer agente de IA** (Claude, Gemini, Antigravity, Cursor, Copilot) e qualquer desenvolvedor humano, atuando desde a concepção e planejamento da tarefa até a abertura do Pull Request.

---

## 🏛️ Invariantes Críticas de Governança

1. **PROIBIDO CODIFICAR SEM PLANO APROVADO:** Toda demanda deve ser previamente planejada e aprovada pelo usuário antes de qualquer mutação no código.
2. **PROIBIDO CODIFICAR DIRETAMENTE NA `master`:** A branch `master` é o ramo estável de produção. O trabalho ocorre sempre em uma branch temporária criada conforme o **GitHub Flow**.
3. **OBRIGATÓRIO ATUALIZAR `docs/business-rules/<modulo>.md`:** Código e documentação evoluem no mesmo commit. Nenhuma alteração de regra ou fluxo é concluída sem atualizar o respectivo documento em `docs/business-rules/`.
4. **OBRIGATÓRIO GESTÃO DE VERSÃO & `CHANGELOG.md`:** A cada finalização de desenvolvimento, o agente deve questionar o usuário se a entrega fecha uma versão, incrementando os arquivos de build ou registrando sob `[Unreleased]`.
5. **CONVENTIONAL COMMITS MANDATÓRIOS:** Toda mensagem de commit adere estritamente à especificação [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/).
6. **SUBMISSÃO EXCLUSIVA VIA PULL REQUEST:** O fechamento da demanda se dá por abertura de PR para a `master` seguindo as diretrizes de [`.github/pull-request.instructions.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.github/pull-request.instructions.md).

---

## 📋 Fase 1: Protocolo de Planejamento (Plan Mode / `/plan`)

Ao receber uma solicitação do usuário no modo de planejamento (ou comando `/plan`):

1. **Mapeamento de Contexto & Módulos Afetados:**
   - Identifique quais dos 15 Bounded Contexts são impactados (`calendario`, `aprovacao`, `fiel`, `clero`, `projeto`, etc.).
   - **Consulte obrigatoriamente** o documento correspondente em `docs/business-rules/<modulo>.md` para absorver as regras vigentes e não violar invariantes existentes.
2. **Desenho Arquitetural & Técnico:**
   - Especifique as alterações de backend (entidades, use cases, ports, adapters, DDL Flyway) e/ou frontend (componentes, hooks TanStack Query, schemas Zod, Design System).
   - Defina a estratégia de testes unitários/integração (TDD estrito).
3. **Definição da Branch de Trabalho:**
   - Proponha o nome da branch no padrão GitHub Flow: `<tipo>/<descricao-kebab-case>` (ex.: `feat/calculo-dizimo`, `fix/choque-horario-missa`, `chore/atualizar-flyway`).
4. **Previsão de Impacto na Documentação de Negócio:**
   - Liste quais seções de `docs/business-rules/<modulo>.md` serão atualizadas.
5. **Geração do Plano & Solicitação de Aprovação:**
   - Crie o artefato de plano com `RequestFeedback: true` e `UserFacing: true`.
   - **PARE e aguarde aprovação explícita** do usuário antes de criar branches, editar código ou rodar comandos modificadores.

---

## 🚀 Fase 2: Protocolo de Execução Pós-Aprovação (GitHub Flow)

Após o usuário aprovar o plano (ex.: `/plan confirmado`, `/plan faça`, `Proceed`, etc.):

1. **Sincronização com a `master`:**
   ```bash
   git checkout master
   git pull origin master
   ```
2. **Criação da Branch de Trabalho:**
   ```bash
   git checkout -b <tipo>/<descricao-kebab-case>
   ```
3. **Implementação Técnica:**
   - Backend: Clean/Hexagonal Architecture pura em Java 25, sem anotações de framework em `domain/`, entidades estendendo `BaseAuditableEntity`, zero FQCNs inline.
   - Frontend: React 19, TypeScript estrito, MUI 9, estrita obediência ao `DESIGN_SYSTEM.md` (inputs `size="small"`, `<DialogTitle>` limpo, `<form noValidate>`).
   - TDD: Testes unitários com JUnit 5 + AssertJ + Mockito ou Vitest + RTL cobrindo as novas regras.

---

## 📖 Fase 3: Atualização de Living Documentation (`docs/business-rules/`)

Antes de finalizar a implementação:
1. Abra o(s) arquivo(s) `docs/business-rules/<modulo>.md` afetado(s).
2. Atualize o texto para refletir exatamente as novas regras, novos enums, exceções, fluxos ou comportamentos implementados.
3. **Normatividade em Tempo Presente (Zero Histórico):** A redação deve ser estritamente no tempo presente, descrevendo o que o sistema É e COMO ELE FUNCIONA no momento atual. É proibido narrar passado ("o antigo indicador", "foi substituído", "anteriormente era X"). O passado pertence com exclusividade ao `CHANGELOG.md`.
4. Se um novo módulo ou bounded context for criado, adicione um novo documento em `docs/business-rules/` e atualize o índice `docs/business-rules/README.md`.


---

## 🏷️ Fase 4: Protocolo de Finalização, Versão & CHANGELOG.md

Ao concluir o código e passar todos os testes (`npm test`, `./gradlew test`, `npm run build`):

1. **Questionamento Interativo de Fechamento de Versão:**
   - Questione o usuário:
     > *"Este desenvolvimento fecha uma nova versão do sistema (ex.: 0.1.1 ou 0.2.0)?"*
2. **Cenário A - SIM (Fecha Versão):**
   - Determine junto ao usuário o número da nova versão (Patch, Minor ou Major).
   - Incremente a versão no backend em `gestao-paroquia/app/build.gradle.kts` (`version = "X.Y.Z"`).
   - Incremente a versão no frontend em `gestao-paroquia-ui/package.json` (`"version": "X.Y.Z"`).
   - Atualize o `CHANGELOG.md`:
     - Crie a seção `## [X.Y.Z] - AAAA-MM-DD`.
     - Mova as entregas realizadas de `[Unreleased]` para a nova versão, mantendo a seção `[Unreleased]` vazia e pronta para o próximo ciclo.
3. **Cenário B - NÃO (Não Fecha Versão):**
   - Mantenha os arquivos de build na versão corrente.
   - Atualize o `CHANGELOG.md` adicionando os itens desenvolvidos diretamente sob a seção `## [Unreleased]`, categorizados em:
     - `### Adicionado`
     - `### Modificado`
     - `### Corrigido`
     - `### Removido`

---

## ✍️ Fase 5: Conventional Commits

Realize o commit das alterações:
```bash
git add <arquivos-alterados>
git commit -m "<tipo>(<escopo>): <descrição sucinta>" \
           -m "- Detalhe da alteração 1" \
           -m "- Atualização de regras em docs/business-rules/<modulo>.md" \
           -m "- Atualização de CHANGELOG.md"
```

Prefixos válidos: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `build`, `ci`, `chore`.

---

## 🔀 Fase 6: Publicação & Roteiro de Pull Request

1. **Push da Branch para o Repositório Remoto:**
   ```bash
   git push -u origin <tipo>/<descricao-kebab-case>
   ```
2. **Instruções de Pull Request:**
   - Apresente ao usuário as instruções formatadas conforme [`.github/pull-request.instructions.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.github/pull-request.instructions.md).
   - Forneça o título formatado, a descrição preenchida com o checklist de qualidade e o comando para abrir o PR na interface web do GitHub ou via GitHub CLI (`gh pr create`).
3. **Atualização do Grafo de Conhecimento:**
   - Execute `graphify update .` para sincronizar o grafo com todas as novas entidades e arquivos criados.
