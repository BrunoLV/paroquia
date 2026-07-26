# ⚙️ Paróquia - Backend (gestao-paroquia)

API RESTful para o sistema de gestão paroquial.

## 🚀 Tecnologias

- **Linguagem:** Kotlin / Java
- **Framework:** Spring Boot
- **Gerenciador de Dependências:** Gradle
- **Banco de Dados:** PostgreSQL
- **Qualidade de Código:** SonarQube

## 🛠️ Desenvolvimento

### Pré-requisitos

- JDK 17 ou superior (configurado via Gradle Toolchains)
- Docker (para o banco de dados)

### Comandos Úteis

```bash
# Executar testes
./gradlew test

# Rodar a aplicação localmente
./gradlew bootRun

# Gerar build (JAR)
./gradlew build
```

## 📂 Estrutura de Pastas

- `app/`: Módulo principal da aplicação Spring Boot.
- `docker/`: Scripts SQL de inicialização e semente (seed) de dados.

## 📝 Padrões de Código

Consulte o arquivo [GEMINI.md](./GEMINI.md) para diretrizes sobre estilo de código, testes e arquitetura (seguindo
princípios de SRP e Clean Code).
