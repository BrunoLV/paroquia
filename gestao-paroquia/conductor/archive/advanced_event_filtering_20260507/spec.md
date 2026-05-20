# Specification: Advanced Event Filtering & Search

## Goal
Provide flexible and powerful event searching capabilities to allow users to find specific events based on multiple criteria including involved organizations, categories, and custom time ranges.

## Requirements
- Extend `GET /api/v1/eventos` to support:
    - `envolvidoId`: Filter by organization ID present in `envolvidos`.
    - `categoria`: Support multiple categories in a single request.
    - `dataInicio` / `dataFim`: Custom range filtering (beyond existing logic if needed).
    - `status`: Filter by multiple statuses.
- Support pagination for all filtered results.

## Success Criteria
- Users can filter events by an organization that is NOT the responsible one but is an "apoio".
- Complex queries (e.g., all "LITURGICO" events for "Pastoral X" in June) return correct results.
