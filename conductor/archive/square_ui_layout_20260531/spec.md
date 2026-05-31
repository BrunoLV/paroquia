# Specification: Square UI Layout & Component Standardization

## Overview
The user prefers a clean, angulated, and square design system instead of rounded corners. This track aims to configure the Material-UI (MUI) theme to set all component border-radii to `0px` globally, and update any page-level components or custom containers using inline styles or custom styles to maintain a consistent angulated layout.

## Functional Requirements
1. **Global Theme Styling**:
   - Update the Material-UI theme configuration in `theme.ts` to set `shape.borderRadius` to `0` globally.
   - Override specific component border radii in `theme.ts` (e.g. `MuiButton`, `MuiOutlinedInput`, `MuiPaper`, `MuiDialog`, `MuiCard`) to ensure they use `0px`.
2. **Page & Component Styling Alignments**:
   - Scan UI files (like `Login.tsx`, `Projects.tsx`, `Calendar.tsx`, `Approvals.tsx`, `EventRegistrationModal.tsx`) for hardcoded custom inline styles that specify `borderRadius` (e.g. `borderRadius: '16px'` or `borderRadius: '8px'`), and replace or remove them to align with the global `0px` border-radius standard.

## Non-Functional Requirements
- **Consistency**: No rounded buttons, dialog papers, or input outlines should be visible in the application. Everything should render with sharp, square 90-degree corners.

## Acceptance Criteria
- All buttons, text fields, cards, alerts, and dialogs must have exactly `0` or `0px` border radius.
- Existing tests (such as `theme.test.tsx`) are updated to reflect the new square/angulated layout expectations and all tests pass cleanly.

## Out of Scope
- Modifying backend APIs or database schemas.
- Modifying UI layouts or colors that are unrelated to border rounding.
