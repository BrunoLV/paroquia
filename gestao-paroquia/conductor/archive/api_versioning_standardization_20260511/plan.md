# Implementation Plan: Padronização da API do Calendário

## Phase 1: Mover Classes de Erro
- [x] Task: Criar pacote `br.com.nsfatima.gestao.support.api.v1.error`. 380598
- [x] Task: Mover `BaseExceptionHandler`, `ErrorCodes`, `ValidationErrorItem` e `ValidationErrorResponse` para o novo pacote de support. 380598
- [x] Task: Atualizar imports do pacote de error em todo o projeto. 380598

## Phase 2: Mover DTOs e Controllers do Calendário
- [x] Task: Criar pacote `br.com.nsfatima.gestao.calendario.api.v1.dto` e mover todos os subpacotes (`evento`, `metrics`, `support`, `validation`). 381637
- [x] Task: Criar pacote `br.com.nsfatima.gestao.calendario.api.v1.controller` e mover os controllers (`EventoController`, etc). 381637
- [x] Task: Atualizar imports em todo o projeto. 381637
- [x] Task: Rodar `./gradlew test` para garantir que tudo continua funcionando. 382037
- [x] Task: Conductor - User Manual Verification 'Phase 2' (Protocol in workflow.md)
