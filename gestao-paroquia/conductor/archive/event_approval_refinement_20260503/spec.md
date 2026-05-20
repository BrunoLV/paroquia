# Track Specification: Refine Event Approval Workflow

## Description
This track focuses on enhancing the Event Approval system by providing complete API documentation, adding missing functionality like approval listing, and ensuring the flow is robustly tested and error-handled.

## Goals
- Provide comprehensive interactive documentation for the Approval API.
- Implement a new endpoint to list pending and historical approvals.
- Enforce strict validation on all approval-related requests.
- Verify the end-to-end approval lifecycle with integration tests.

## Success Criteria
- `AprovacaoController` is fully documented with OpenAPI.
- A new `GET /api/v1/aprovacoes` endpoint is available and functional.
- Input validation prevents invalid decisions or malformed requests.
- Integration tests cover the full lifecycle: request, list, and decide.
