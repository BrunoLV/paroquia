# Plan: Correção de Inconsistências do Conductor

### Phase 0 — Setup
- [x] Task: Preparar branch de trabalho
  - [x] Confirmar que a working tree estava em `master`
  - [x] Criar e mudar para a branch `chore/conductor-config-fixes-20260804` a partir de `master`

### Phase 1 — Corrigir `conductor.yml` e residual de track
- [x] Task: Corrigir referência quebrada ao arquivo de LGPD em `conductor.yml`
  - [x] Substituir `regras_duras_lgpd.md` por `regra_dura_lgpd.md` na regra `pre_spec`
  - [x] Verificar: `grep -n "regra" conductor.yml` aponta para o caminho correto e o arquivo existe
- [x] Task: Remover diretório residual de track já arquivada
  - [x] Confirmar que `conductor/tracks/motor_liturgico_universal_eclesial_20260731/` é idêntico ao conteúdo em `conductor/archive/` (`diff -rq`)
  - [x] Remover `conductor/tracks/motor_liturgico_universal_eclesial_20260731/`
  - [x] Verificar: `conductor/tracks/` fica sem tracks ativas residuais; `conductor/archive/` permanece intacto
- [x] Task: Verificação e checkpoint da Fase 1 (conforme workflow.md)

### Phase 2 — Atualizar `conductor/tech-stack.md`
- [x] Task: Remover `@mui/x-charts` da lista de dependências ativas
  - [x] Editar a seção "UI Component Library & Styling" removendo a referência
  - [x] Verificar: `grep -n "x-charts" conductor/tech-stack.md` só aparece na nota histórica de migração, não como dependência ativa
- [x] Task: Remover `ThemeContext` da lista de Context API
  - [x] Editar a seção "State Management & Data Fetching → Context API" para listar apenas `AuthContext` e `NotificationContext`
  - [x] Verificar: `grep -n "ThemeContext" conductor/tech-stack.md` não retorna resultados
- [x] Task: Verificação e checkpoint da Fase 2 (conforme workflow.md)

### Phase 3 — Atualizar `conductor/product.md`
- [x] Task: Adicionar módulos de Patrimônio e Conselho às Core Features
  - [x] Adicionar bullet descrevendo o módulo de Patrimônio (ativos, alocação/reserva, manutenção)
  - [x] Adicionar bullet descrevendo o módulo de Conselho (decisões/homologação do CPC)
  - [x] Verificar: `grep -n "Patrimônio\|Conselho" conductor/product.md` retorna as novas entradas
- [x] Task: Verificação e checkpoint da Fase 3 (conforme workflow.md)

### Phase 4 — Encerramento
- [x] Task: Revisão final de todos os arquivos alterados (`git diff master`)
- [x] Task: Commit das alterações na branch `chore/conductor-config-fixes-20260804`
- [x] Task: Verificação e checkpoint final da track (conforme workflow.md)
