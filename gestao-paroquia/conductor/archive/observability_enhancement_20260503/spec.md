# Track Specification: Enhance Observability and Auditing

## Description
This track focuses on standardizing and documenting the observability module. It includes providing a complete OpenAPI specification for auditing and metrics endpoints, ensuring robust validation for reporting parameters, and verifying that all critical business operations are properly audited.

## Goals
- Provide full interactive documentation for the Auditing API.
- Ensure all metrics and audit DTOs are well-defined and validated.
- Verify the integrity of the audit trail for core business events.
- Implement comprehensive integration tests for metrics retrieval.

## Success Criteria
- `AuditoriaEventoController` is fully documented with OpenAPI.
- Audit trail queries support flexible and validated filtering.
- Integration tests verify that Event, Project, and Approval operations emit correct audit logs.
- Metrics endpoints return consistent and documented responses.
