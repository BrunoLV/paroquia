# Specification: Event Categories Enum

## Overview
Refactor the event categorization system to use a predefined Java Enum instead of a database table. This simplifies the architecture and introduces standard, well-defined categories for parish events.

## Functional Requirements
1.  **Event Categories:** Define a new `CategoriaEvento` Enum with the following values:
    *   `PASTORAL`
    *   `SOCIAL`
    *   `LITURGICO`
    *   `ADMINISTRATIVO`: Reuniões de conselho, fechamento de caixa, assembleias.
    *   `SACRAMENTAL`: Batismos, casamentos, crismas.
    *   `FORMATIVO`: Catequese, cursos de noivos, grupos de estudo bíblico.
    *   `ASSISTENCIAL`: Campanhas de caridade, entrega de cestas básicas, sopão.
2.  **API DTOs:** Update all event-related request and response DTOs to accept and return the direct string representation of the new Enum values.
3.  **Database Migration:**
    *   Create a Flyway migration to drop the existing `categorias` table.
    *   Replace the `categoria_id` column in the `eventos` table with a new `categoria` column (VARCHAR) to store the Enum string representation.
    *   (Optional) Migrate existing `categoria_id` references to their corresponding Enum string values before dropping the table, assuming basic mappings exist for existing data.

## Non-Functional Requirements
*   **Simplicity:** The categorization is purely informational and filtering-oriented, without attaching complex business rules to specific categories at this stage.

## Acceptance Criteria
*   The `categorias` table is successfully dropped via a Flyway migration.
*   The `eventos` table stores category information as strings matching the `CategoriaEvento` Enum.
*   API clients can create, update, and retrieve events using the exact Enum string values.
*   The application starts and existing tests (updated for the new Enum) pass.
*   Any existing event filtering by category works with the new string-based Enum logic.