# Diretrizes e Instruções para Abertura de Pull Requests (PR)

Este guia define o padrão institucional de mercado para a abertura, revisão e aprovação de Pull Requests direcionados para a branch `master` no repositório **Projeto Paróquia**.

---

## 1. Padrão de Nomenclatura da Branch (GitHub Flow)

Toda branch deve ser criada a partir da `master` mais recente e seguir rigorosamente a convenção:
`<tipo>/<descricao-curta-kebab-case>`

- `feature/` ou `feat/`: Novas funcionalidades ou telas (ex.: `feat/modulo-batismo`).
- `fix/`: Correções de bugs ou falhas de negócio (ex.: `fix/choque-horario-missa`).
- `refactor/`: Refatorações sem alteração de comportamento externo (ex.: `refactor/simplificar-hooks-calendario`).
- `chore/`: Tarefas de build, infraestrutura, dependências ou documentação (ex.: `chore/setup-fluxo-dev`).
- `test/`: Adição ou melhoria de suites de testes (ex.: `test/cobertura-service-fiel`).

---

## 2. Padrão de Título do Pull Request (Conventional Commits)

O título do Pull Request deve seguir a especificação [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0/):
`<tipo>(<escopo>): <descrição sucinta em minúsculas>`

Exemplos:
- `feat(calendario): adicionar exportacao de apresentacao litúrgica em pptx`
- `fix(painel): corrigir ordenacao de projetos sob risco no dashboard`
- `docs(business-rules): atualizar regras de exclusividade de pároco no clero`

---

## 3. Template Obrigatório de Descrição do Pull Request

Ao abrir o PR, copie e preencha a seguinte estrutura:

```markdown
## 📌 Contexto e Motivação
Descreva sucintamente o problema resolvido, o objetivo de negócio ou a motivação desta entrega.

## 🛠️ O Que Foi Alterado (Solução Técnica)
- Resumo dos componentes backend, frontend, scripts SQL ou arquivos de configuração modificados.
- Decisões de design relevantes (ex.: criação de Policy, ajuste de schema, novo hook).

## 🏛️ Bounded Contexts Impactados
- [ ] aprovacao
- [ ] calendario
- [ ] clero
- [ ] conselho
- [ ] fiel
- [ ] governanca
- [ ] iam
- [ ] importacao
- [ ] local
- [ ] membro
- [ ] organizacao
- [ ] painel / dashboard
- [ ] patrimonio
- [ ] projeto
- [ ] reserva

## ✅ Checklist de Qualidade e Governança
- [ ] **Branching:** Branch criada a partir de `master` atualizada conforme GitHub Flow.
- [ ] **Testes Automatizados:** Testes unitários/integração escritos e 100% passando (`npm test` e/ou `./gradlew test`).
- [ ] **Build:** Compilação limpa sem erros (`npm run build` e/ou `./gradlew check`).
- [ ] **Living Documentation:** Regras de negócio e fluxos atualizados em `docs/business-rules/<modulo>.md`.
- [ ] **Changelog & Versão:** Registrado no `CHANGELOG.md` (sob `[Unreleased]` ou nova versão).
- [ ] **LGPD & Segurança:** Zero PII exposta em logs; endpoints com `@PreAuthorize`.
- [ ] **Design System (se houver UI):** Aderência estrita ao `gestao-paroquia-ui/DESIGN_SYSTEM.md` (inputs `size="small"`, `<DialogTitle>` limpo, sem tags HTML cruas para layout).

## 📸 Evidências / Screenshots (Obrigatório para Frontend)
- [ ] **Listas pesquisáveis:** Campos, filtros, seleção múltipla e Linhas por página seguem a seção 2.1 do Design System; suíte estrutural do frontend passando.
- [ ] **Rodapé das tabelas:** Paginação dentro do `Table`, com `colSpan` correto; Linhas por página e anterior/próximo com mesma altura, sem afetar formulários. Suíte `tablePaginationArchitecture.test.ts` passando.
- [ ] **Ações de linha:** `TableActionMenu` pequeno delineado `⋮`, sem texto visível, inclusive ação única; acessibilidade, permissões e estados preservados. Suítes `TableActionMenu.test.tsx` e `tableActionsArchitecture.test.ts` passando.
*Insira capturas de tela ou gravações demonstrando o comportamento visual e validações.*
```

---

## 4. Critérios de Aprovação e Merge
1. **Zero Quebra de Build:** Todos os pipelines de CI/CD devem estar verdes.
2. **Revisão Aprovada:** Pelo menos um revisor (líder técnico ou mantenedor) deve homologar as alterações.
3. **Estratégia de Merge:** Prefira **Squash and Merge** com a mensagem formatada no padrão Conventional Commits para manter a história da `master` linear e limpa.
