# Track Specification: Full Implementation of Project Concept

## Overview
This track aims to fully implement the 'Projeto' (Project) concept, transforming it from a simple catalog to a primary management entity. Projects will have responsible organizations, specific timeframes, and will serve as collaborative anchors for events from various pastorals.

## Functional Requirements
- **Project Data Evolution**: Add `organizacao_responsavel_id`, `inicio_utc`, and `fim_utc` to projects.
- **Event-Project Link**: Add `projeto_id` to events to establish a collaborative relationship.
- **Recurrence Exclusion**: Ensure events linked to projects cannot be recurring.
- **Temporal Validation**: Events must occur within the project's start and end dates.
- **Collaborative Access**: Allow events from different pastorals to be linked to the same project.
- **Security**: Apply the same actor-based authorization rules as events to projects.

## Non-Functional Requirements
- Maintain data integrity via Foreign Keys.
- Ensure type safety using Enums for status and roles.
- High auditability for all project-related changes.
