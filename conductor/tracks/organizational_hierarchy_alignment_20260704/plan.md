# Implementation Plan: Organizational Hierarchy Alignment

## Phase 1: Database Verification & Seed Update [checkpoint: ]
- [ ] Task: Ensure all seeded Matriz/Comunidade organizations are linked to the Clero parent.
    - [ ] Update SQL seed script `seed-data.sql` to link `MATRIZ` and `COMUNIDADE` organizations to the `CLERO` root organization.
- [ ] Task: Conductor - User Manual Verification 'Database Verification & Seed Update' (Protocol in workflow.md)

## Phase 2: Backend Development (TDD) [checkpoint: ]
- [ ] Task: Red Phase - Write Backend Unit Tests
    - [ ] Add unit tests in `GetOrganogramaUseCaseTest` to verify that the organograma tree is generated dynamically using `parentId` relationships.
    - [ ] Add unit tests in `CreateOrganizacaoUseCaseTest` / `UpdateOrganizacaoUseCaseTest` to verify hierarchy validation rules (e.g. Matriz/Comunidade must have Clero as parent; Councils must have Matriz/Comunidade as parent; Pastorais must have Pastoral Councils as parent).
- [ ] Task: Green Phase - Implement Usecase & Validation Logic
    - [ ] Rewrite `GetOrganogramaUseCase.java` to dynamically build the recursive tree from `parentId` linkages in the database.
    - [ ] Implement hierarchy validation rules in organization creation and update use cases.
- [ ] Task: Conductor - User Manual Verification 'Backend Implementation' (Protocol in workflow.md)

## Phase 3: Frontend Development (TDD) [checkpoint: ]
- [ ] Task: Red Phase - Write Frontend Unit Tests
    - [ ] Add unit tests in `OrganizacaoFormModal.test.tsx` verifying that parent-child inputs filter options dynamically based on the selected organization type.
    - [ ] Add unit tests in `Organograma.test.tsx` verifying that recursive structures are rendered correctly.
- [ ] Task: Green Phase - Implement UI Changes
    - [ ] Add a **Vínculo (Pai)** dropdown selector in `OrganizacaoFormModal.tsx`.
    - [ ] Implement dynamic filtering of parent options in `OrganizacaoFormModal.tsx` based on the selected organization type.
    - [ ] Verify that `Settings.tsx` passes all necessary list collections (e.g., all active organizations) to populate parent options.
- [ ] Task: Conductor - User Manual Verification 'Frontend Implementation' (Protocol in workflow.md)
