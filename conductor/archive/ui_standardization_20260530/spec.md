# Specification: UI Standardization & Unified Error Presentation

## Overview
This track aims to standardize the visual presentation and error/validation reporting across all user interfaces of the Paróquia system. By leveraging the existing Material-UI (MUI) ThemeProvider, we will ensure consistent typography, icons, buttons, links, and forms. Error presentation will be unified globally using the floating Toast (Snackbar) notification system.

## Functional Requirements
1. **Global Styling Theme Configuration**:
   - Customize and enforce consistent styling rules in the React application's MUI Theme (font-family, base colors, spacing, borders).
   - Use standard MUI fonts (Roboto/Inter/System) integrated into the core theme.
2. **Unified Component Appearance**:
   - **Buttons & Links**: Enforce consistent button variants (`contained`, `outlined`, `text`), hover transitions, micro-animations, and consistent link styling (underline, hover color, integration with MUI Typography).
   - **Forms**: Enforce standard form styling including consistent input margins, sizes, borders, labels, and padding.
   - **Icons**: Standardize the usage of `@mui/icons-material` for action controls and navigation (e.g., Edit, Cancel, Save, Info, Delete, Menu, Logout, Settings).
3. **Unified Error & Validation Presentation**:
   - Validation errors (both client-side inputs and backend response validations) must be displayed exclusively via the floating Toast (Snackbar) provider (`NotificationContext`).
   - Eliminate verbose, inconsistent alert elements or custom message boxes within forms, replacing them with standard calls to `showError(message)`.

## Non-Functional Requirements
- **Consistency**: The user interface must feel cohesive and professional.
- **Micro-interactions**: Subtle hover states, shadow adjustments, and transitions to convey premium design aesthetics.
- **Accessibility**: Enforce color contrast ratios and appropriate ARIA attributes for inputs and icon buttons.

## Acceptance Criteria
- Form inputs across `Calendar.tsx`, `Projects.tsx`, `Approvals.tsx`, and `Login.tsx` share identical margins, heights, typography, and border treatments.
- All form-related validation errors (e.g., missing required fields, duplicated names, invalid credentials) trigger a floating Toast/Snackbar alert.
- Standardized icons are used consistently across pages.
- Standardized buttons and links behave identically.

## Out of Scope
- Backend logic modifications (except mapping validation exceptions to standard responses if necessary).
- Redesigning the entire dashboard dashboard cards content layout, only standardizing their shared components (buttons, fonts, borders).
