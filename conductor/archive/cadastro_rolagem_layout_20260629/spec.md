# Specification: Fix Layout Scroll on Login and Signup Screens

## Overview
This track fixes a UI layout bug on the `/login` and `/login` (signup) screens. Currently, when the user switches to the user registration form, the form grows taller than the viewport height on smaller screens. Because the layout doesn't support vertical scrolling properly, parts of the form and action buttons at the bottom are cut off, making it impossible to complete the registration.

## Requirements
- Wrap the Login page container in a layout element that spans `minHeight: '100vh'`.
- Align items dynamically in the center (vertically and horizontally).
- Apply `py: 4` padding to guarantee safe margins and enable standard browser window scrolling when card height exceeds the viewport.
- Apply this centered full-viewport layout wrapper consistently to both the Login and SignUp states.
- Clean up any legacy or ad-hoc vertical alignments that might conflict with vertical flex behavior.

## Acceptance Criteria
- Toggling between Login and SignUp states keeps the card centered on the page.
- When the SignUp form exceeds the viewport height (especially on screen heights < 850px), vertical scrollbars appear and allow full form navigation.
- All input fields, selections, and submit buttons remain fully accessible.
- Built package compiles with success.
