# Specification: Visual Theme Adjustment (Nossa Senhora de Fátima)

## Overview
Ajustar a identidade visual da aplicação `gestao-paroquia-ui` incorporando as cores tradicionais associadas à iconografia de Nossa Senhora de Fátima: Azul (Virgindade/Céu) como cor primária, Dourado (Realeza/Glória) como cor secundária, Vermelho (Amor/Espírito Santo/Coroação) para alertas e ações críticas, e Branco/Amarelo do Vaticano como base para fundos e superfícies. O design deve se manter totalmente angulado e quadrado (border-radius `0px`) conforme o padrão estabelecido.

## Functional Requirements
1. **Theme Palette Integration**:
   - **Primary Color (Blue)**: Implement a deep, rich Marian blue (e.g., `#1A237E`) as the primary theme color. It will be used for AppBars, primary buttons, active UI states, and text links.
   - **Secondary Color (Gold)**: Implement a rich, readable gold color (e.g., `#C59B27`) as the secondary theme color. It will be used for secondary buttons, highlights, badges, and accents.
   - **Error/Alert Color (Red)**: Implement a rich crimson red (e.g., `#C62828`) for `error` states, critical alerts, and destructive actions (e.g., event deletion/cancellation).
   - **Background & Surfaces (White/Light Yellow)**: The default background of the application should use white (`#FFFFFF`) or a very subtle warm light-yellow/cream tint (e.g., `#FFFDF4`) as the page background to align with the Vatican colors and Marian theme, while maintaining high readability.
2. **Component Custom Styling**:
   - Verify that all default Material-UI components (Alerts, Buttons, TextFields, Cards) inherit the new color variables cleanly.
   - Ensure the square border-radius constraint (`borderRadius: 0` / `0px`) remains untouched.

## Non-Functional Requirements
- **Contrast and Accessibility**: All color combinations must meet WCAG 2.1 AA contrast requirements (at least 4.5:1 for normal text). Specifically, white text on blue/red backgrounds and dark text on gold backgrounds must be highly readable.
- **Global Theme Application**: The styling changes must be defined in the global MUI theme (`theme.ts`) to avoid styling fragmentation.

## Acceptance Criteria
- Primary actions, AppBars, and major headings are colored in Marian Blue.
- Secondary accents and minor highlights are colored in Gold.
- The `error` state (e.g., in alerts and cancellation buttons) is styled with the rich Crimson Red.
- Backgrounds are light, using a white or light cream palette with high text contrast.
- No rounded borders are introduced (border-radius remains exactly `0`).
- The application passes all automated unit and UI compilation checks.

## Out of Scope
- Modifying backend APIs or database models.
- Changing font families or structural UI layouts unrelated to color theme styling.
