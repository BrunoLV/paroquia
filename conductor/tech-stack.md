# Tech Stack

## Primary Language
- **TypeScript:** Strongly typed language built on JavaScript, providing excellent developer experience, improved maintainability, and robust type safety for the React application.

## Core Framework & Bundler
- **React (via Vite):** The foundational library for building the user interface, bundled and served using Vite for lightning-fast HMR, instant startup, and modern ES module builds.

## UI Component Library & Styling
- **Material UI:** A comprehensive suite of pre-built React components that follow Google's Material Design guidelines, enabling rapid development of a professional, consistent, and accessible interface.

## State Management & Data Fetching
- **React Query:** Specifically utilized for managing server state, handling data fetching, caching, synchronization, and updates with the `@gestao-paroquia` API.
- **Axios:** Used as the base HTTP client for making API requests, configured with withCredentials enabled for cookie-based session management and interceptors for 401 handling.
- **Context API:** Used for managing simple, application-wide global UI state (like theme preferences, authenticated user details, and active roles) without the overhead of complex external libraries.

## Routing
- **React Router (v6):** Used for client-side routing, managing navigation between pages (e.g., Login, Dashboard) and implementing protected routes.

## Testing Framework
- **Vitest:** Blazing fast unit and component test runner natively integrated with Vite, jsdom, and React Testing Library.
