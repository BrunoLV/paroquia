# Specification: Padronização da API do Calendário

## 1. Overview
O módulo `calendario` foi o primeiro a ser desenvolvido e sua estrutura de pacotes para a camada de API (`br.com.nsfatima.gestao.calendario.api.*`) não reflete a versão da API REST (v1), ao contrário dos módulos mais recentes (`iam`, `local`, etc). Esta especificação define a renomeação destes pacotes para manter a consistência em todo o monólito.

## 2. Escopo
*   Mover `br.com.nsfatima.gestao.calendario.api.controller` para `br.com.nsfatima.gestao.calendario.api.v1.controller`.
*   Mover `br.com.nsfatima.gestao.calendario.api.dto.*` para `br.com.nsfatima.gestao.calendario.api.v1.dto.*`.
*   Mover `br.com.nsfatima.gestao.calendario.api.error` para `br.com.nsfatima.gestao.support.api.v1.error` (pois são classes base de erro usadas globalmente).

## 3. Critérios de Aceite
*   [ ] Nenhuma classe do pacote `api` raiz do calendário.
*   [ ] Todos os imports atualizados e o projeto compilando.
*   [ ] Testes executando com sucesso.