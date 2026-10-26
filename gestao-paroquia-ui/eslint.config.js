/**
 * ESLint (flat config) do frontend.
 *
 * Duas famílias de regras:
 * 1. Higiene geral (typescript-eslint recommended, react-hooks, jsx-a11y recommended).
 * 2. Fronteiras DDD/Hexagonal do AGENTS.md §4.1, via `no-restricted-imports`:
 *    - `domain/` e `application/` não conhecem React, MUI, TanStack Query, Zod nem Axios;
 *    - Axios só vive em `infrastructure/` (adapters HTTP de cada contexto e `shared/infrastructure`).
 *
 * Regras rebaixadas a `warn` abaixo cobrem dívida legada medida na introdução do lint; cada uma
 * tem o motivo ao lado e deve voltar a `error` quando o backlog zerar (o CI não bloqueia em warn).
 */
import js from '@eslint/js';
import globals from 'globals';
import tseslint from 'typescript-eslint';
import reactHooks from 'eslint-plugin-react-hooks';
import jsxA11y from 'eslint-plugin-jsx-a11y';
import prettier from 'eslint-config-prettier';

const bibliotecasDeUi = [
  'react',
  'react-dom',
  'react-dom/*',
  '@mui/*',
  '@tanstack/*',
  'zod',
  'axios',
  'react-hook-form',
];

const restringirUi = (camada) => ({
  patterns: [
    {
      group: bibliotecasDeUi,
      message: `${camada}/ é TypeScript puro: sem React, MUI, TanStack Query, Zod ou Axios (AGENTS.md §4.1).`,
    },
  ],
});

export default tseslint.config(
  { ignores: ['dist/**', 'coverage/**', 'node_modules/**', 'public/**'] },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  jsxA11y.flatConfigs.recommended,
  {
    files: ['**/*.{ts,tsx}'],
    languageOptions: { globals: { ...globals.browser, ...globals.node } },
    plugins: { 'react-hooks': reactHooks },
    rules: {
      ...reactHooks.configs.recommended.rules,
      'no-console': ['error', { allow: ['warn', 'error'] }],
      // Legado: catch vazio intencional (best effort em logout/mapeamentos); o resto de no-empty continua erro.
      'no-empty': ['error', { allowEmptyCatch: true }],
      // Legado: try/catch que só relança em Aprovacoes/AuthContext/useOrganogramaExport; limpar em refactor dedicado.
      'no-useless-catch': 'warn',
      // autoFocus em campo principal de Dialog é decisão de UX do Design System (foco inicial previsível);
      // aviso mantém visibilidade sem bloquear. Revisar contra DESIGN_SYSTEM.md antes de voltar a error.
      'jsx-a11y/no-autofocus': 'warn',
      // Legado: any/unknown sem narrowing é proibido pelo AGENTS.md, mas ainda existe em código antigo.
      '@typescript-eslint/no-explicit-any': 'warn',
      // Legado: variáveis e catch não usados em código antigo; limpar gradualmente.
      '@typescript-eslint/no-unused-vars': ['warn', { argsIgnorePattern: '^_', varsIgnorePattern: '^_' }],
      // Axios só em infrastructure (adapters de contexto e shared/infrastructure).
      'no-restricted-imports': [
        'error',
        {
          paths: [{ name: 'axios', message: 'Axios é exclusivo de infrastructure/ (AGENTS.md §4.1).' }],
        },
      ],
    },
  },
  {
    files: ['src/**/infrastructure/**/*.{ts,tsx}', 'src/**/*.test.{ts,tsx}', 'src/app/setupTests.ts'],
    rules: { 'no-restricted-imports': 'off' },
  },
  {
    files: ['src/contextos/*/domain/**/*.{ts,tsx}'],
    ignores: ['**/*.test.{ts,tsx}'],
    rules: { 'no-restricted-imports': ['error', restringirUi('domain')] },
  },
  {
    files: ['src/contextos/*/application/**/*.{ts,tsx}'],
    ignores: ['**/*.test.{ts,tsx}'],
    rules: { 'no-restricted-imports': ['error', restringirUi('application')] },
  },
  {
    files: ['**/*.test.{ts,tsx}', 'src/app/setupTests.ts'],
    rules: { '@typescript-eslint/no-explicit-any': 'off' },
  },
  {
    // O logger estruturado é o único sink autorizado de console.* (AGENTS.md: sem console.log fora dele).
    files: ['src/shared/infrastructure/logging/logger.ts'],
    rules: { 'no-console': 'off' },
  },
  {
    files: ['*.config.{js,ts}'],
    languageOptions: { globals: globals.node },
  },
  prettier
);
