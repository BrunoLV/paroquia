# Regra Mandatória: Fluxo de Desenvolvimento do Sistema (Plan & Execution)

Esta regra é **ALWAYS-ON (PERMANENTE)** e de cumprimento **ESTRITAMENTE OBRIGATÓRIO** para qualquer agente de IA (Claude, Gemini, Antigravity, Cursor, Copilot) ou desenvolvedor humano em qualquer modo de operação (*Plan Mode*, *Think Mode*, *Execution Mode* ou prompt interativo). É **TERMINANTEMENTE PROIBIDO IGNORAR OU CONTORNAR ESTA REGRA**.

---

## 1. Atuação no Modo Planejamento (*Plan Mode* / `/plan`)

Antes de gerar código ou propor alterações:
1. **Consulta Mandatória a `docs/business-rules/`:**
   - O agente **DEVE OBRIGATORIAMENTE** ler o documento do módulo afetado em `docs/business-rules/<modulo>.md` antes de formular a solução.
2. **Estruturação do Plano Técnico:**
   - Detalhe a arquitetura (DDD, Hexagonal, Clean Code, Design System, LGPD).
   - Especifique a branch no padrão **GitHub Flow** (`<tipo>/<descricao-kebab-case>`).
   - Liste quais regras de negócio em `docs/business-rules/<modulo>.md` serão criadas ou atualizadas.
   - Detalhe os testes automatizados que garantirão a entrega.
3. **Bloqueio de Execução sem Aprovação:**
   - É **EXPRESSAMENTE PROIBIDO** executar mutações no código, rodar scripts de migração ou criar arquivos antes do usuário aprovar explicitamente o plano.

---

## 2. Atuação no Modo Execução / Implementação (Pós-Aprovação)

Após a aprovação expressa do plano pelo usuário:

### 2.1. GitHub Flow Mandatório
1. Acesse a branch `master` e faça pull do último estado:
   ```bash
   git checkout master && git pull origin master
   ```
2. Crie uma branch semântica de trabalho a partir da `master`:
   ```bash
   git checkout -b <tipo>/<descricao-kebab-case>
   ```
   *É expressamente proibido desenvolver ou commitar diretamente na branch `master`.*

### 2.2. Living Documentation em `docs/business-rules/`
- Cada módulo do sistema possui seu manual em `docs/business-rules/<modulo>.md`.
- **Invariante de Atualização:** Todo código implementado que altere comportamentos, entidades, enums, políticas ou telas **DEVE OBRIGATORIAMENTE atualizar o documento correspondente em `docs/business-rules/` no mesmo ciclo de desenvolvimento**, mantendo a documentação sincronizada com o último estado implementado.
- **Invariante Temporal (Zero Histórico):** A documentação em `docs/business-rules/` é estritamente normativa e em tempo presente. É proibido narrar passado, o que existia antes ou citar termos como "antigo", "foi substituído". O histórico pertence exclusivamente ao `CHANGELOG.md`.


### 2.3. Gestão de Versão & CHANGELOG.md
- O projeto mantém o histórico central em `CHANGELOG.md` e a versão base inicial é `0.1.0`.
- **Pergunta Obrigatória de Fechamento de Versão:** Ao concluir a implementação e testes, o agente **DEVE QUESTIONAR O USUÁRIO**:
  > *"Este desenvolvimento fecha alguma versão do sistema (ex.: 0.1.1 ou 0.2.0)?"*
- **Se afirmativo:**
  1. Incremente a versão no backend (`gestao-paroquia/app/build.gradle.kts` -> `version = "X.Y.Z"`).
  2. Incremente a versão no frontend (`gestao-paroquia-ui/package.json` -> `"version": "X.Y.Z"`).
  3. Atualize o `CHANGELOG.md` criando a nova seção `## [X.Y.Z] - AAAA-MM-DD` e migrando os itens de `[Unreleased]`.
- **Se negativo:**
  1. Mantenha os arquivos de build na versão corrente.
  2. Registre as alterações desenvolvidas diretamente sob a seção `## [Unreleased]` do `CHANGELOG.md`.

### 2.4. Conventional Commits
- Todos os commits devem seguir a especificação [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/):
  - Formato: `<tipo>(<escopo>): <descrição>`
  - Tipos válidos: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`, `build`, `ci`, `perf`, `style`.

### 2.5. Submissão Exclusiva via Pull Request para `master`
1. Publique a branch no repositório remoto:
   ```bash
   git push -u origin <tipo>/<descricao-kebab-case>
   ```
2. Apresente ao usuário as instruções estruturadas para abertura de Pull Request direcionado para a `master`, em estrito acordo com o padrão institucional em [`.github/pull-request.instructions.md`](file:///home/bruno-luiz-viana/DEV/WORKSPACE/JAVA/paroquia/.github/pull-request.instructions.md).
