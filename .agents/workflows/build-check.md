---
name: build-check
description: "Executa a suíte completa de compilação, testes e verificações estáticas de qualidade do monorepo (Backend e Frontend)."
---

# Workflow: Verificação Completa de Build e Qualidade

Execute os seguintes passos para garantir conformidade estrita com a política de Zero Warnings e integridade do monorepo:

## 1. Backend (`gestao-paroquia/`)

Execute a partir do diretório `gestao-paroquia/`:

```bash
# 1.1. Verificação estrita de qualidade estática (Checkstyle, SpotBugs, PMD)
./gradlew check

# 1.2. Execução da suíte de testes unitários e de arquitetura (ArchUnit)
./gradlew test

# 1.3. Execução direcionada apenas da validação arquitetural ArchUnit
./gradlew test --tests "br.com.nsfatima.gestao.architecture.*"

# 1.4. Compilação completa e geração de artefatos
./gradlew build -x test
```

> **Critério de Aceite:** O build deve finalizar com `BUILD SUCCESSFUL`, 100% de conformidade com as regras do ArchUnit e zero warnings de compilação ou violação de estilo.

---

## 2. Frontend (`gestao-paroquia-ui/`)

Execute a partir do diretório `gestao-paroquia-ui/`:

```bash
# 2.1. Checagem estrita de tipos TypeScript e build de produção
npm run build

# 2.2. Execução da suíte de testes automatizados com Vitest
npm test
```

> **Critério de Aceite:** Zero erros de tipagem no `tsc`, zero erros no ESLint e 100% de testes passando no Vitest.

---

## 3. Atualização de AST (Graphify)

Após alterações de código e validação dos builds, execute na raiz do monorepo:

```bash
graphify update .
```
