# Skill do Agente de IA: Automação e Construção com Gradle

## 1. Visão Geral e Contexto

Esta skill capacita o Agente de IA a gerar, estruturar, refatorar e otimizar scripts de automação de compilação utilizando o **Gradle Build Tool**. Todo o código gerado e as decisões arquiteturais devem basear-se rigorosamente nas práticas recomendadas pela **Documentação Oficial do Gradle**, com foco absoluto em desempenho (Performance), modularidade e manutenibilidade.

## 2. Princípios Fundamentais de Performance do Gradle

Ao configurar qualquer script ou estrutura de build, o Agente deve habilitar e respeitar os seguintes mecanismos de otimização nativos:

* **Configuration Cache (Cache de Configuração):** Evite computar o gráfico de tarefas em toda execução. O código dos plugins e das tarefas customizadas não deve acessar o estado do projeto em tempo de execução de tarefas.


* **Build Cache (Cache de Build):** Reutilize saídas de tarefas de compilações anteriores ou de outras máquinas (CI). Certifique-se de que todas as tarefas customizadas tenham suas entradas (`@Input`) e saídas (`@Output`) explicitamente anotadas para permitir a incrementalidade.


* **Task Configuration Avoidance (Evitação de Configuração de Tarefas):** Nunca utilize `project.tasks.create(...)` (criação imediata). Use estritamente `tasks.register(...)` para que a tarefa só seja configurada se for efetivamente executada no grafo atual.


* **Daemon & File System Watching:** Garanta que as configurações facilitem o uso do Gradle Daemon e o monitoramento do sistema de arquivos para builds subsequentes ultra-rápidos.



## 3. Idioma e Tecnologias Padrão

* **DSL:** O Agente deve escrever scripts utilizando exclusivamente **Kotlin DSL (`.gradle.kts`)**, em conformidade com a recomendação moderna do Gradle para tipagem estática e melhor suporte a IDEs. Evite Groovy, a menos que explicitamente solicitado.


* **Gradle Wrapper:** Toda instrução de execução deve utilizar o Wrapper (`./gradlew` ou `gradlew.bat`), garantindo que o build seja reprodutível e independente da versão instalada na máquina local.



## 4. Gerenciamento de Dependências Moderno

O Agente deve abolir declarações de versões espalhadas pelos submódulos. Toda gestão de dependências deve seguir os padrões modernos de centralização:

1. **Version Catalogs (Catálogo de Versões):** Centralize todas as declarações de dependências e plugins no arquivo `gradle/libs.versions.toml`.


2. **Java Toolchains:** Defina explicitamente a versão do ecossistema Java via Toolchain no bloco `java` para desacoplar a versão de execução do Gradle da versão de compilação da aplicação.



## 5. Blueprint Estrutural (Exemplo de Projeto Multi-Module Limpo)

O agente deve gerar a estrutura de arquivos e configurações focando em **Convention Plugins** (compartilhamento de lógica de build) em vez do legado `subprojects {}` ou `allprojects {}`.

### Arquivo: `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
    // Habilita o Catálogo de Versões centralizado
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "sistema-corporativo"
include("app")
include("core")

```

### Arquivo: `gradle/libs.versions.toml`

```toml
[versions]
java = "17"
springBoot = "3.2.0"

[libraries]
spring-boot-starter = { module = "org.springframework.boot:spring-boot-starter", version.ref = "springBoot" }
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version = "5.10.1" }

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "springBoot" }

```

### Arquivo: `app/build.gradle.kts`

```kotlin
plugins {
    java
    alias(libs.plugins.spring.boot)
}

java {
    // Configuração recomendada de Java Toolchain
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.java.get()))
    }
}

dependencies {
    implementation(project(":core"))
    implementation(libs.spring.boot.starter)
    
    testImplementation(libs.junit-jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform() // Garante o uso do JUnit 5
}

```

### Arquivo: `gradle.properties` (Configurações Globais de Performance)



```properties
# Ativação dos recursos de performance da documentação oficial
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.parallel=true
org.gradle.vfs.watch=true
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m

```

## 6. Checklist de Revisão e Anti-Patterns a Evitar

Antes de entregar qualquer script Gradle, o Agente deve validar:

* **Uso de `allprojects` ou `subprojects`:** O script faz uso desses blocos no root? *(Se sim, refatore usando Convention Plugins ou buildSrc para evitar acoplamento mutuamente prejudicial entre subprojetos)*.


* **Configuração Eager de Tarefas:** Existe o uso de `tasks.create` ou declarações diretas de blocos fora do escopo preguiçoso? *(Substitua por `tasks.register` e utilize Lazy Configuration com `Property<T>` ou `Provider<T>`)*.


* **Hardcoded Versions:** Há strings de versões diretamente no arquivo `build.gradle.kts`? *(Mova imediatamente para o `libs.versions.toml`)*.


* **Falta de Reprodutibilidade:** Repositórios dinâmicos ou declarações de dependências com versões `latest.release` ou `+` estão sendo utilizadas? *(Force versões estáticas e imutáveis)*.