# Specification: Visual Theme Customization (Nossa Senhora de Fátima)

## Overview
Update the `gestao-paroquia-ui` application's visual theme to reflect the identity of the "Nossa Senhora de Fátima" parish. This involves configuring the Material UI theme palette, typography, and updating visual assets to create a cohesive, Marian-inspired aesthetic while maintaining modern usability standards.

## Functional Requirements
- **Color Palette Update:**
  - **Primary Color:** Marian Blue (e.g., `#1A237E` or similar deep blue) to represent grace and the traditional Marian mantle. Used for AppBars, primary buttons, and active states.
  - **Secondary/Accent Color:** Gold / Yellow (e.g., `#FFB300`) to represent light, crown, and purity. Used for floating action buttons, highlights, and secondary actions.
  - **Background & Surface:** Maintain clean, legible backgrounds (whites and light grays) to ensure content readability.
- **Typography:**
  - Maintain a "Modern & Clean" aesthetic using standard, highly legible sans-serif fonts (e.g., Roboto, Inter).
- **Visual Assets:**
  - Update any placeholder logos or favicons with the official parish logo or Marian icons (if provided/available).

## Non-Functional Requirements
- **Accessibility:** Ensure the new color combinations meet WCAG 2.1 AA standards for contrast ratios (e.g., white text on Marian Blue background).
- **Consistency:** Apply the theme globally via the Material UI `ThemeProvider` so all components inherit the new styles automatically.

## Acceptance Criteria
- [ ] The `theme.ts` file is updated with the new Primary (Marian Blue) and Secondary (Gold/Yellow) colors.
- [ ] The application's AppBar, primary buttons, and key UI elements reflect the new Marian Blue color.
- [ ] Secondary actions and highlights reflect the Gold/Yellow accent color.
- [ ] Color contrast is verified and passes accessibility checks.
- [ ] (If applicable) Favicon, logo, and title tags are updated to reference "Paróquia Nossa Senhora de Fátima".

## Out of Scope
- Complete redesign of page layouts or structural components.
- Creating the graphic design assets (logos/icons) from scratch (we will use existing/provided assets or standard Material icons).