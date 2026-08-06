# Plan: Regressão Final e Fechamento da Iniciativa SDD

## Phase 0: Setup
- [x] Task: Criar branch nova a partir da `master` atualizada (`git pull` + `git checkout -b
      chore/sdd-regressao-final-<data>`)
- [x] Task: Confirmar que as Tracks 1-6 estão implementadas (backend e frontend) — se não estiverem, HALT e
      avisar o usuário
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 1: Cenário 1 — Cadastro sem login
- [x] Task: Escrever teste de integração (Red) que cadastra um Fiel, adiciona-o como membro de um
      GrupoParoquial via `AdicionarMembroUseCase`, e verifica explicitamente que nenhuma linha foi criada em
      `usuarios`
- [x] Task: Ajustar o que for necessário para o teste passar (Green) — expectativa é que já passe, dado que
      as Tracks 1-3 implementaram o comportamento; qualquer falha aqui indica regressão a corrigir
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 2: Cenário 2 — Consulta cruzando Fiel + Usuario
- [x] Task: Escrever teste de integração (Red) que lista os membros de um GrupoParoquial e verifica que as
      lideranças (coordenador/vice) são corretamente indicadas cruzando `fielId` com `papel_global`/
      `MembroGrupo`
- [x] Task: Ajustar o que for necessário para o teste passar (Green)
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 3: Cenário 3 — Acesso irrestrito do Vigário
- [x] Task: Escrever teste de integração (Red) que autentica um Usuario com Fiel `VIGARIO` em `papel_global`
      e verifica acesso/edição de um GrupoParoquial `CONSELHO` sem vínculo prévio como membro
- [x] Task: Ajustar o que for necessário para o teste passar (Green)
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 4: Varredura e remoção de código órfão
- [x] Task: Levantar (grep/análise de uso) todos os aliases de compatibilidade remanescentes em enums
      renomeados/removidos nas Tracks 4/5
- [x] Task: Para cada alias sem uso real: escrever teste (Red) confirmando que removê-lo não quebra nada
      conhecido, remover (Green), documentar no commit
- [x] Task: Para aliases ainda em uso: documentar o motivo no Javadoc/comentário correspondente
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 5: Sincronização de documentação de projeto
- [x] Task: Atualizar `conductor/product.md` e `conductor/tech-stack.md` com o estado final do domínio
- [x] Task: Atualizar `gestao-paroquia/AGENTS.md` e skills relevantes (`.agents/skills/ddd.md` se aplicável)
- [x] Task: Atualizar `gestao-paroquia-ui/.agents/skills/react-frontend.md` (ou equivalente) se ainda não
      cobrir os fluxos da Track 6
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)

## Phase 6: Regressão completa e fechamento
- [x] Task: Rodar a suíte completa de backend (`./gradlew test`) e confirmar 100% de sucesso
- [x] Task: Rodar a suíte completa de frontend (`CI=true npm test` + `npx tsc --noEmit`) e confirmar 100% de
      sucesso
- [x] Task: Arquivar as 7 tracks da iniciativa (mover `conductor/tracks/*_20260804/` desta iniciativa para
      `conductor/archive/`, atualizar `tracks.md`)
- [x] Task: Phase Verification & Checkpoint (Refer to workflow.md)
