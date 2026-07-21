# Specification: Parish Organizational Hierarchy Alignment

## 1. Overview
Align the system's organizational hierarchy and organograma generation to strictly reflect the canonical parish structure:
- **Clero** (Root node).
- **Paróquia (Matriz) & Comunidades** (Children of Clero).
- **Conselho Pastoral & Conselho Financeiro (Econômico)** (Children of Paróquia/Comunidade).
- **Pastorais & Movimentos** (Children of Conselho Pastoral).

The organograma tree will be computed dynamically using the database `parent_id` relationships rather than static type classification. The user interface will offer the ability to link parent organizations dynamically with context-aware validation.

## 2. Functional Requirements
- **Dynamic Tree Building**: Update the backend `GetOrganogramaUseCase` to traverse and assemble the organograma recursively using `parentId` relationships.
- **UI Form Integration**:
  - Add a **Vínculo / Organização Pai** (Parent Organization) dropdown inside the `OrganizacaoFormModal` dialog.
  - Dynamically filter options based on the selected organization type:
    - **MATRIZ / COMUNIDADE**: parent options are `CLERO` organizations.
    - **CONSELHO / CONSELHO_ECONOMICO**: parent options are `MATRIZ` or `COMUNIDADE` organizations.
    - **PASTORAL / MOVIMENTO**: parent options are `CONSELHO` (Pastoral) organizations.
- **Validation**:
  - Backend validation in `CreateOrganizacaoUseCase` / `UpdateOrganizacaoUseCase` to ensure the parent organization is active and matches the type hierarchy rules.

## 3. Non-Functional Requirements
- Maintain existing API backward-compatibility.
- Standard WCAG AA contrast and UI consistency (0px border-radius).

## 4. Acceptance Criteria
- Organograma page renders the recursive tree starting from Clero down to individual members.
- Creating/editing an organization in Settings displays the Parent selector with appropriate type-based filtering.
- Backend tests cover the validation rules for parent-child organization associations.
