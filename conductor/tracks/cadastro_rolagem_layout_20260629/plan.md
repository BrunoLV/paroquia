# Implementation Plan: Fix Layout Scroll on Login and Signup Screens

## Fase 1: Escrita de Testes (Red Phase)
- [ ] Task: Escrita de Testes Unitários de Layout
    - [ ] Adicionar teste em `Login.test.tsx` verificando a presença do wrapper com estilos de centralização vertical, altura mínima `100vh` e suporte a rolagem.

## Fase 2: Ajuste de Layout na UI (Green Phase)
- [ ] Task: Refatoração do Componente Login
    - [ ] Adicionar wrapper `Box` com `minHeight: "100vh"`, flex centrado, background e padding vertical no retorno de `Login.tsx`.
    - [ ] Ajustar o `Container` interno para remover o `marginTop: 8` fixo que impacta na centralização vertical.
- [ ] Task: Validação e Compilação
    - [ ] Garantir que todos os testes unitários passem.
    - [ ] Executar o build de produção (`npm run build`) para verificar a ausência de erros.

## Fase 3: Verificação e Validação
- [ ] Task: Conductor - User Manual Verification 'Interface do Usuário' (Protocol in workflow.md)
