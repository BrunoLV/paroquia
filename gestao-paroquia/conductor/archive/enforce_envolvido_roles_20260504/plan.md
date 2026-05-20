# Implementation Plan: Enforce Envolvido Roles

## Phase 1: Domain and Infrastructure Alignment [checkpoint: e8b2258]
- [x] Task: Create `PapelEnvolvido` enum in `br.com.nsfatima.calendario.domain.type`. e803f15
- [x] Task: Update `EventoEnvolvidoEntity` to use `PapelEnvolvido` enum. 296123a
- [x] Task: Update `EventoEnvolvidoInput` DTO to use `PapelEnvolvido` enum. 2013d63
- [x] Task: Update `EventoEnvolvidosResponse` DTO to use `PapelEnvolvido` enum. d6049d1
- [x] Task: Update `UpdateEventoEnvolvidosUseCase` to handle the enum conversion. e0314a5
- [x] Task: Update tests to use the new enum and verify validation. e3df6b6
- [x] Task: Conductor - User Manual Verification 'Phase 1: Domain and Infrastructure Alignment' (Protocol in workflow.md) e8b2258
