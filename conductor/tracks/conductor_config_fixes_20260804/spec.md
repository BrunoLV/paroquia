# Spec: Correção de Inconsistências do Conductor

## Overview
Track de manutenção (chore) para corrigir referências quebradas e informações desatualizadas na configuração e documentação do Conductor (`conductor.yml`, `conductor/tech-stack.md`, `conductor/product.md`, `conductor/tracks/`), trazendo-as de volta à conformidade com o estado real do código. Nenhuma mudança de comportamento do sistema é envolvida.

## Functional Requirements

1. **`conductor.yml`** — corrigir a regra `pre_spec` que referencia `gestao-paroquia/docs/ia/regras_duras_lgpd.md` (plural) para o caminho real `gestao-paroquia/docs/ia/regra_dura_lgpd.md` (singular).
2. **`conductor/tracks/motor_liturgico_universal_eclesial_20260731/`** — remover este diretório residual (conteúdo idêntico ao já arquivado em `conductor/archive/`, track já marcada `[x]` em `tracks.md`).
3. **`conductor/tech-stack.md`** — na seção "Frontend Stack → UI Component Library & Styling", remover a menção a `@mui/x-charts` (dependência totalmente removida do código, sem referências em `src/` ou `package.json`), mantendo apenas a nota histórica de migração já existente no documento.
4. **`conductor/tech-stack.md`** — na seção "State Management & Data Fetching → Context API", remover `ThemeContext` da lista (não existe no código; o tema é configurado em `theme.ts` sem Context próprio), mantendo `AuthContext` e `NotificationContext`.
5. **`conductor/product.md`** — na seção "Core Features", adicionar menção aos módulos de **Patrimônio** (cadastro de ativos, alocação/reserva por evento, manutenção) e **Conselho** (decisões e homologação do CPC), ambos implementados no backend e com telas dedicadas no frontend.

## Non-Functional Requirements
- Nenhuma alteração de código de produção (`gestao-paroquia/`, `gestao-paroquia-ui/`) — escopo restrito a `conductor.yml` e `conductor/*.md`.
- Manter o formato e estilo dos documentos existentes (não reescrever seções não afetadas).
- **Estratégia de branch:** todo o desenvolvimento desta track deve ocorrer em uma branch dedicada criada a partir de `master` (`chore/conductor-config-fixes-20260804`), nunca diretamente sobre `master`. A branch foi criada como primeira tarefa do `plan.md`, antes de qualquer edição de arquivo.

## Acceptance Criteria
- [ ] Branch dedicada criada a partir de `master` antes de qualquer alteração.
- [ ] `conductor.yml` referencia `regra_dura_lgpd.md` (caminho válido, arquivo existe).
- [ ] `conductor/tracks/motor_liturgico_universal_eclesial_20260731/` não existe mais; `conductor/archive/motor_liturgico_universal_eclesial_20260731/` permanece intacto.
- [ ] `conductor/tech-stack.md` não contém mais `@mui/x-charts` como dependência ativa.
- [ ] `conductor/tech-stack.md` não contém mais `ThemeContext`.
- [ ] `conductor/product.md` menciona Patrimônio e Conselho em "Core Features".
- [ ] `conductor/tracks.md` recebe a entrada desta própria track ao final do processo.

## Out of Scope
- Revisão de `workflow.md`, `product-guidelines.md` ou dos `code_styleguides/` dos subprojetos.
- Qualquer alteração de código em `gestao-paroquia/` ou `gestao-paroquia-ui/`.
- Novas features ou correções de bugs no sistema.
- Merge da branch de volta para `master` (fica a critério do usuário, fora do escopo automatizado desta track).
