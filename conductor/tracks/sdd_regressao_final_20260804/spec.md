# Spec: Regressão Final e Fechamento da Iniciativa SDD

## Overview

Track 7 de 7 (última) da iniciativa de aderência ao SDD de domínio. Pressupõe Tracks 1-6 já implementadas:
backend e frontend refletem `Fiel`/`GrupoParoquial`/`UnidadeTerritorial`/`papel_global`/`MembroGrupo` de
ponta a ponta.

Esta é a track de fechamento: valida explicitamente, via testes, que os 3 cenários de comportamento (BDD) da
spec de domínio original realmente funcionam ponta a ponta após as 6 tracks anteriores; varre e remove
código/aliases legados que ficaram órfãos ao longo da migração; sincroniza a documentação de projeto
(`product.md`, `tech-stack.md`, `AGENTS.md`) com o estado final do domínio; e roda uma regressão completa
(backend + frontend) confirmando a coerência de toda a iniciativa.

## Functional Requirements

- **FR1 — Cenário 1 (Cadastro sem login):** teste de integração no módulo `fiel`/`membro` comprovando que um
  `Fiel` pode ser cadastrado e adicionado como membro de um `GrupoParoquial` via `AdicionarMembroUseCase` sem
  que nenhuma linha seja criada em `usuarios` — reforça o AC2 já coberto na Track 3, agora como parte da
  suíte de regressão final ponta a ponta.
- **FR2 — Cenário 2 (Consulta cruzando Fiel + Usuario):** teste de integração no módulo `organizacao`/`membro`
  que lista os membros de um `GrupoParoquial` e confirma que a lista é construída a partir de `Fiel`, com as
  lideranças (coordenador/vice) corretamente indicadas cruzando `fielId` com `papel_global`/`MembroGrupo`.
- **FR3 — Cenário 3 (Acesso irrestrito do Vigário):** teste de integração no módulo `iam`/`organizacao`
  comprovando que um `Usuario` cujo `Fiel` tem papel `VIGARIO` em `papel_global` acessa/edita um
  `GrupoParoquial` do tipo `CONSELHO` sem nenhum vínculo prévio como membro desse conselho — cobre
  explicitamente o gap do Vigário identificado e corrigido na Track 4.
- **FR4 — Varredura de código órfão:** levantamento (via grep/análise de uso) de aliases de compatibilidade
  legados que não têm mais nenhum caller real após as Tracks 1-6 (ex.: aliases de `fromStoredValue`/
  `fromJson` remanescentes de enums renomeados/removidos nas Tracks 4/5). Cada alias encontrado sem uso é
  removido; aliases ainda referenciados em dados/testes existentes são documentados e mantidos.
- **FR5 — Sincronização de documentação de projeto:** `conductor/product.md` e `conductor/tech-stack.md`
  (monorepo) e `gestao-paroquia/AGENTS.md` (e `.agents/skills/` relevantes, incluindo
  `gestao-paroquia-ui/.agents/skills/react-frontend.md` se aplicável) atualizados para descrever o estado
  final do domínio: `Fiel`/`Vocacao`, `GrupoParoquial`/`TipoGrupo`/`UnidadeTerritorial`, `MembroGrupo`,
  `papel_global`, e a separação Contexto Paroquial vs. Contexto IAM.
- **FR6 — Regressão completa:** suíte completa de backend (`./gradlew test`) e frontend (`CI=true npm test`
  + `npx tsc --noEmit`) executada e confirmada 100% verde, como checkpoint final de toda a iniciativa de 7
  tracks.

## Non-Functional Requirements

- **NFR1:** Os 3 testes de cenário (FR1-FR3) são escritos como testes de integração de alto nível (não
  mocks pesados), próximos ao módulo mais relevante de cada cenário, seguindo o padrão de testes de
  integração já usado no projeto.
- **NFR2:** Nenhuma remoção de código em FR4 quebra teste existente — cada remoção é feita com o próprio
  ciclo Red/Green (confirmar que nada mais usa o alias antes de remover).

## Acceptance Criteria

1. Existem 3 testes de integração passando, um para cada cenário BDD da spec original (Cenários 1, 2 e 3),
   localizados nos módulos mais relevantes.
2. Nenhum alias de compatibilidade sem uso real permanece no código; os que restam estão documentados com o
   motivo de ainda existirem.
3. `product.md`, `tech-stack.md` e os `AGENTS.md`/skills relevantes descrevem corretamente o domínio final
   (Fiel, GrupoParoquial, UnidadeTerritorial, papel_global, MembroGrupo).
4. `./gradlew test` (backend) e `CI=true npm test` + `npx tsc --noEmit` (frontend) passam 100%, sem
   regressões.
5. A iniciativa completa (Tracks 1-7) é arquivada em `conductor/archive/` ao final desta track.

## Out of Scope

- Qualquer nova funcionalidade além da validação/limpeza/documentação descrita acima.
- Mudanças de comportamento de negócio — esta track só confirma o que as 6 anteriores já implementaram.
