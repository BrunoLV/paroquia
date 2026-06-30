# Implementation Plan: Fix Layout Scroll on Login and Signup Screens

## Fase 1: Escrita de Testes (Red Phase)
- [x] Task: Escrita de Testes Unitários de Layout [108c028]
    - [x] Adicionar teste em `Login.test.tsx` verificando a presença do wrapper com estilos de centralização vertical, altura mínima `100vh` e suporte a rolagem.

## Fase 2: Ajuste de Layout na UI (Green Phase)
- [x] Task: Refatoração do Componente Login [4bed4a2]
    - [x] Adicionar wrapper `Box` com `minHeight: "100vh"`, flex centrado, background e padding vertical no retorno de `Login.tsx`.
    - [x] Ajustar o `Container` interno para remover o `marginTop: 8` fixo que impacta na centralização vertical.
- [x] Task: Validação e Compilação [4bed4a2]
    - [x] Garantir que todos os testes unitários passem.
    - [x] Executar o build de produção (`npm run build`) para verificar a ausência de erros.

## Fase 3: Verificação e Validação
- [x] Task: Conductor - User Manual Verification 'Interface do Usuário' (Protocol in workflow.md) [4bed4a2]
