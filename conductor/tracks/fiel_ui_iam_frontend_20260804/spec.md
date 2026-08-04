# Spec: Adaptação do Frontend a Fiel/GrupoParoquial/IAM

## Overview

Track 6 de 7 da iniciativa de aderência ao SDD de domínio. Pressupõe Tracks 1-5 já implementadas: o backend
`gestao-paroquia` expõe `Fiel`, `GrupoParoquial`/`UnidadeTerritorial`, `MembroGrupo`, `papel_global`, os
casos de uso `AdicionarMembroUseCase`/`PromoverACoordenadorUseCase`, e o autocadastro público
(`/usuarios/registrar`) foi removido.

Esta track adapta `gestao-paroquia-ui` para voltar a funcionar de ponta a ponta com os novos contratos:
cadastro de Fiel sem exigir login, seleção de um Fiel existente ao criar um Usuario, promoção a Coordenador
restrita por papel, e remoção do fluxo de autocadastro público quebrado desde a Track 2.

## Functional Requirements

- **FR1 — Nova tela `/fieis`:** página dedicada de gestão de Fiéis (listagem + cadastro/edição), acessível a
  staff/secretaria autenticado, seguindo o padrão visual e de densidade já usado em outras páginas de gestão
  do sistema (ex.: `Ativos.tsx`, `Settings.tsx`). Formulário de cadastro/edição cobre os campos de contato do
  Fiel (nome, cpf, dataNascimento, email, telefone, endereco, vocacao) — nunca pede username/senha.
- **FR2 — Botão "Promover a Coordenador":** em cada linha de Fiel já membro de algum grupo (via
  `MembroGrupo`), um botão condicional, visível apenas quando o usuário autenticado possui um dos 4 papéis
  globais permitidos (`PAROCO`, `VIGARIO`, `MESA_CPP`, `ADMIN` — mesma checagem que o backend aplica),
  seguindo o padrão de botões condicionais por papel já usado em outras telas (ex.: `Aprovacoes.tsx`). Abre um
  modal que pede papel de liderança + credenciais (username/senha) apenas se o Fiel ainda não tiver Usuario
  vinculado.
- **FR3 — Cadastro de Usuário via seleção de Fiel existente:** o formulário "Cadastrar Usuário" em
  `Settings.tsx` passa a ter um seletor/busca de Fiel já cadastrado (consultando `/fieis`) em vez de campos de
  dados pessoais soltos, mais username/senha — refletindo o novo contrato de `CreateUsuarioUseCase`
  (Track 2).
- **FR4 — Remoção do autocadastro público em `Login.tsx`:** remover o toggle "Cadastro de Usuário"
  (`isRegistering`) e a chamada `POST /usuarios/registrar`. `Login.tsx` volta a ser exclusivamente tela de
  login.
- **FR5 — Terminologia `GrupoParoquial`/`UnidadeTerritorial`:** telas e labels que hoje referenciam
  "Organização" (Settings.tsx, formulários de vínculo, menus) são atualizadas para refletir a nomenclatura
  pós-Track 5 (`GrupoParoquial` para grupos pastorais/conselhos, `UnidadeTerritorial` para Matriz/Comunidade/
  Clero), incluindo os componentes `OrganizacaoFormModal`, `LocalFormModal` (se aplicável) e as rotas/labels
  de menu em `Layout.tsx`.
- **FR6 — Ajuste dos clients de API:** `api/usuarios.ts`, `api/membros.ts`, `api/organizacoes.ts` (renomeado
  para refletir `GrupoParoquial`) são atualizados para os novos formatos de request/response de cada
  endpoint alterado nas Tracks 1-5, incluindo um novo `api/fieis.ts` para a tela de FR1.
- **FR7 — Adicionar membro comum (Cenário 1 da spec original):** a interface para adicionar um Fiel como
  membro de um `GrupoParoquial` (papel comum, sem promoção) é exposta em algum ponto da UI — reaproveitando
  o fluxo de vínculo já existente em `OrganizacaoFormModal`/telas de grupo, adaptado para selecionar um Fiel
  em vez de um Usuario.

## Non-Functional Requirements

- **NFR1:** Segue os padrões visuais já estabelecidos (Marian Blue/Gold, `0px` border-radius, mapeamento
  ação→cor→ícone da track de estilização de botões já concluída).
- **NFR2:** Testes de componente (Vitest/RTL) cobrindo a nova tela `/fieis`, o botão condicional de FR2, e o
  formulário ajustado de FR3, seguindo o padrão de teste já usado no restante do projeto.
- **NFR3:** Verificação manual via `npm run dev` para os fluxos de FR1-FR4, já que não há ferramenta de
  browser automatizado neste ambiente — a checagem de UI fica a cargo do usuário durante a implementação.

## Acceptance Criteria

1. Existe uma tela `/fieis` funcional para listar e cadastrar/editar Fiéis sem pedir dados de login.
2. O botão "Promover a Coordenador" só aparece para usuários com papel global permitido, e cria/reaproveita
   o Usuario conforme o backend espera.
3. "Cadastrar Usuário" em Settings.tsx seleciona um Fiel existente em vez de digitar dados pessoais.
4. `Login.tsx` não tem mais nenhum caminho de autocadastro público.
5. Nenhuma tela referencia mais "Organização" onde o conceito correto é `GrupoParoquial`/`UnidadeTerritorial`.
6. Suíte completa de testes do frontend (`CI=true npm test`) passa sem regressões; `npx tsc --noEmit` limpo.
7. `gestao-paroquia-ui`/skills relevantes (`.agents/skills/react-frontend.md`) atualizados descrevendo os
   novos fluxos de Fiel/promoção.

## Out of Scope

- Qualquer mudança de regra de negócio no backend — esta track só consome os contratos já definidos nas
  Tracks 1-5.
- Novo endpoint público de autocadastro de Fiel (decidido como fora de escopo nas Tracks 2 e 6).
- Redesenho visual amplo além do necessário para a nova tela e ajustes de terminologia.
