# Plan: Visual Theme Adjustment (Nossa Senhora de Fátima)

## Phase 1: Configure Theme Palette in theme.ts
- [ ] Task: Update the theme unit tests in `theme.test.tsx` to assert new Primary (Blue), Secondary (Gold), Error (Crimson Red), and Background (Light/Cream) colors
- [ ] Task: Modify the Material-UI theme configuration in `theme.ts` with the new color palette mappings
- [ ] Task: Verify that unit tests for the theme pass cleanly (TDD green phase)
- [ ] Task: Conductor - User Manual Verification 'Phase 1: Configure Theme Palette in theme.ts' (Protocol in workflow.md)

## Phase 2: Review and Adjust Component Color Applications
- [ ] Task: Scan components for hardcoded legacy color styling (such as older blue or amber/yellow overrides) and align them with theme colors
- [ ] Task: Execute the complete test suite to ensure no styling or runtime regressions are introduced
- [ ] Task: Conductor - User Manual Verification 'Phase 2: Review and Adjust Component Color Applications' (Protocol in workflow.md)
