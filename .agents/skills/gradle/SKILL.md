---
name: gradle
description: "Automação e engenharia de build com Gradle: Kotlin DSL (.gradle.kts), Java 25 Toolchain, Version Catalogs libs.versions.toml, Convention Plugins em build-logic e Lazy API."
---

# Skill do Agente de IA: Automação, Engenharia de Build e Performance com Gradle (Java 25)

## 1. Visão Geral e Contexto

Esta skill capacita o Agente de IA a projetar, refatorar, otimizar e auditar a infraestrutura de build e automação de projetos utilizando o **Gradle Build Tool** (versões 8.10+ e 9+), com compilação e execução estritas em **Java 25**. O agente atua como um Engenheiro de Build e Arquiteto de Plataforma Sênior, garantindo builds herméticos, reprodutíveis, paralelizados e com velocidade extrema em ambientes locais e de Integração Contínua (CI).

O agente deve abolir padrões legados da era Groovy (`allprojects`, `subprojects`, `buildscript`, `tasks.create`, dependências dinâmicas) e adotar o paradigma moderno de **Convention Plugins em `build-logic**`, **Version Catalogs (`libs.versions.toml`)**, **Lazy Configuration API**, isolamento total de **Configuration Cache** e configuração avançada de **Java Toolchains**.

---

## 2. O Build como Documentação Viva (*Living Build & Platform Specs*)

A lógica de automação do build é código de produção de primeira classe e deve expressar explicitamente as intenções arquiteturais e as restrições da plataforma.

* **Cabeçalho Conceitual Obrigatório em Scripts e Plugins de Convenção:** Todo arquivo de convenção (`build-logic/`) ou script de módulo deve documentar as garantias de compilação, o propósito dos plugins aplicados e os requisitos de compatibilidade:
```kotlin
/**
 * CONVENÇÃO DE BUILD: paroquia.java-common-conventions.gradle.kts
 * MÓDULO: Plataforma / Build Logic
 *
 * PROPÓSITO:
 * Padroniza os módulos da aplicação estabelecendo o Java 25 Toolchain,
 * flags de compilação do javac para parâmetros de reflexão e otimizações de JVM.
 *
 * GARANTIAS DE PERFORMANCE:
 * - Lazy Task Configuration em todas as tarefas de compilação e teste.
 * - Habilita o runner da plataforma JUnit 5 nativo com execução paralela por forks.
 */

```


* **Proibição de Comentários Triviais:** Comentários como `// Adiciona dependência` ou `// Bloco de repositórios` são proibidos. Documente flags de compilador não óbvias, exclusões de transitividade ou opções avançadas de runtime.

---

## 3. Pilares Fundamentais de Performance Extrema do Gradle

Ao estruturar qualquer build, o agente deve garantir a aderência aos quatro pilares de aceleração do motor Gradle:

### 3.1. Configuration Cache (Cache de Configuração)

* O Gradle armazena em disco o grafo computado de tarefas e o reutiliza imediatamente se as entradas do build não mudarem, reduzindo o tempo de inicialização a milissegundos.
* **Proibição Absoluta de Acesso ao Modelo do Projeto em Tempo de Execução:** Tarefas customizadas NUNCA devem referenciar `project` dentro de `doLast {}` ou em métodos anotados com `@TaskAction`. Todas as dependências e arquivos de entrada devem ser passados como propriedades lazies tipadas (`Property<T>`, `DirectoryProperty`, `RegularFileProperty`).

### 3.2. Build Cache & Incrementalidade Rígida

* Tarefas que geram saídas em disco devem ser anotadas com `@CacheableTask` (quando viável) e declarar minuciosamente suas entradas (`@Input`, `@InputFiles`, `@InputDirectory`) e saídas (`@OutputFile`, `@OutputDirectory`).
* Todas as entradas de arquivos devem declarar a estratégia de caminho apropriada (`@PathSensitive(PathSensitivity.RELATIVE)` ou `PathSensitivity.NAME_ONLY`) para permitir o compartilhamento de cache entre máquinas com diretórios raiz diferentes (CI vs. Local).

### 3.3. Task Configuration Avoidance (Lazy API Universal)

* **Nunca** utilize `tasks.create(...)` ou referencie tarefas via resolução gulosa (`tasks.getByName(...)`).
* Use exclusivamente a **Lazy API**:
* Registro de tarefas: `tasks.register<Tipo>("nome") { ... }`
* Configuração de tarefas existentes: `tasks.named<Tipo>("nome") { ... }`
* Associação de valores entre tarefas via `Provider<T>` / `Property<T>` com mapeamentos lazy: `taskA.outputFile.flatMap { ... }`.



### 3.4. Paralelismo e Isolamento do Sistema de Arquivos

* O build deve ser projetado para execução paralela de submódulos desacoplados (`org.gradle.parallel=true`) e monitoramento dinâmico de alterações (`org.gradle.vfs.watch=true`).

---

## 4. Convenções Modernas de Linguagem e Gerenciamento de Dependências

### 4.1. Kotlin DSL Idiomático (`.gradle.kts`)

* O uso de scripts em Groovy (`.gradle`) é terminantemente proibido.
* Utilize APIs com tipagem estática e *type-safe accessors* gerados pelo Kotlin DSL.

### 4.2. Catálogo Centralizado de Versões (`libs.versions.toml`)

* Versões, dependências, conjuntos de dependências (*bundles*) e plugins devem ser declarados exclusivamente no arquivo `gradle/libs.versions.toml`.
* É expressamente proibido declarar coordenadas literais de dependências (`"org.slf4j:slf4j-api:2.0.9"`) ou versões hardcoded nos arquivos `build.gradle.kts`.
* **Proibição de Versões Dinâmicas:** Nunca utilize `+`, `latest.release` ou ranges de versão dinâmicos que quebram o determinismo e a reprodutibilidade do build.

### 4.3. Java Toolchains Desacoplado (Java 25)

* Desacople a JVM que executa o daemon do Gradle da JVM que compila o código-fonte da aplicação.
* A compilação e os testes devem ser fixados no **Java 25** via Toolchain:
```kotlin
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

```



---

## 5. Arquitetura Modular Baseada em Convention Plugins (`build-logic`)

O agente deve abolir blocos imperativos e altamente acoplados como `allprojects {}` ou `subprojects {}` no `settings.gradle.kts` ou no arquivo raiz. O compartilhamento de lógica de compilação deve ser feito via **Convention Plugins** implementados em um composite build localizado em `build-logic`.

### 5.1. Topologia de Diretórios do Projeto

```
projeto-raiz/
├── build-logic/                          <-- Composite Build de Lógica de Compilação
│   ├── settings.gradle.kts
│   ├── build.gradle.kts
│   └── src/main/kotlin/
│       ├── paroquia.java-common-conventions.gradle.kts
│       ├── paroquia.library-conventions.gradle.kts
│       └── paroquia.application-conventions.gradle.kts
├── gradle/
│   ├── wrapper/
│   │   ├── gradle-wrapper.jar
│   │   └── gradle-wrapper.properties
│   └── libs.versions.toml               <-- Catálogo Central de Dependências
├── core/                                <-- Submódulo de Negócio / Domínio (Java 25 puro)
│   └── build.gradle.kts
├── app/                                 <-- Submódulo Executável (Spring Boot / Java 25)
│   └── build.gradle.kts
├── gradle.properties                    <-- Otimizações Globais do Daemon
└── settings.gradle.kts                  <-- Orquestrador do Multi-Project

```

---

## 6. Blueprints Estruturais e Arquivos de Configuração

### 6.1. `gradle/libs.versions.toml`

```toml
[versions]
java = "25"
springBoot = "3.4.0"
springDependencyManagement = "1.1.7"
flyway = "10.19.0"
junitJupiter = "5.11.3"
assertj = "3.26.3"
mockito = "5.14.2"

[libraries]
# Spring Boot Core
spring-boot-starter = { module = "org.springframework.boot:spring-boot-starter" }
spring-boot-starter-web = { module = "org.springframework.boot:spring-boot-starter-web" }
spring-boot-starter-data-jpa = { module = "org.springframework.boot:spring-boot-starter-data-jpa" }

# Database & Migration
flyway-core = { module = "org.flywaydb:flyway-core", version.ref = "flyway" }
flyway-database-postgresql = { module = "org.flywaydb:flyway-database-postgresql", version.ref = "flyway" }

# Testing Stack
junit-jupiter = { module = "org.junit.jupiter:junit-jupiter", version.ref = "junitJupiter" }
assertj-core = { module = "org.assertj:assertj-core", version.ref = "assertj" }
mockito-core = { module = "org.mockito:mockito-core", version.ref = "mockito" }
mockito-junit-jupiter = { module = "org.mockito:mockito-junit-jupiter", version.ref = "mockito" }
junit-platform-launcher = { module = "org.junit.platform:junit-platform-launcher" }

[bundles]
testing = ["junit-jupiter", "assertj-core", "mockito-core", "mockito-junit-jupiter"]

[plugins]
spring-boot = { id = "org.springframework.boot", version.ref = "springBoot" }
spring-dependency-management = { id = "io.spring.dependency-management", version.ref = "springDependencyManagement" }

```

### 6.2. `gradle.properties` (Aceleração e Otimização do Daemon)

```properties
# Ativação dos recursos de alta performance do Gradle
org.gradle.caching=true
org.gradle.configuration-cache=true
org.gradle.parallel=true
org.gradle.vfs.watch=true

# Alocação de memória do compilador e daemon
org.gradle.jvmargs=-Xmx3g -XX:MaxMetaspaceSize=768m -XX:+UseZGC

# Isolamento e previsibilidade
org.gradle.warning.mode=all

```

### 6.3. `settings.gradle.kts` (Raiz)

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
}

rootProject.name = "gestao-paroquia"

// Inclusão do composite build de convenções de automação
includeBuild("build-logic")

// Módulos da aplicação
include("core")
include("app")

```

### 6.4. `build-logic/settings.gradle.kts`

```kotlin
dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

```

### 6.5. `build-logic/build.gradle.kts`

```kotlin
plugins {
    `kotlin-dsl`
}

dependencies {
    // Permite que os Convention Plugins manipulem plugins de terceiros de forma fortemente tipada
    compileOnly(libs.plugins.spring.boot.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
    compileOnly(libs.plugins.spring.dependency.management.map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" })
}

```

### 6.6. `build-logic/src/main/kotlin/paroquia.java-common-conventions.gradle.kts`

```kotlin
plugins {
    java
}

val libs = extensions.getByType<VersionCatalog>().named("libs")

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.findVersion("java").get().requiredVersion))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(
        listOf(
            "-Xlint:all",
            "-Xlint:-processing",
            "-parameters" // Preserva os metadados de parâmetros para reflexão e injeção do Spring
        )
    )
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    
    // Paralelismo de testes por forks de processos
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1)
    
    testLogging {
        events("passed", "skipped", "failed")
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

dependencies {
    // Fornece o JUnit Platform Launcher como runtimeOnly para todos os módulos
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

```

### 6.7. `app/build.gradle.kts` (Exemplo de Módulo Executável Limpo)

```kotlin
plugins {
    id("paroquia.java-common-conventions")
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency-management)
}

dependencies {
    implementation(project(":core"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    testImplementation(libs.bundles.testing)
}

```

---

## 7. Tarefas Customizadas e o Modelo Lazy (Configuration Cache Compliant)

Quando o agente for requisitado a criar tarefas customizadas de automação (ex.: exportar schemas, empacotar artefatos ou validar arquivos de migração), deve seguir estritamente o modelo de **Lazy Configuration com anotações de I/O tipadas**:

```kotlin
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*

/**
 * Tarefa customizada para geração de metadados de build.
 * Projetada em conformidade estrita com o Configuration Cache e incrementalidade.
 */
@CacheableTask
abstract class GerarMetadadosBuildTask : DefaultTask() {

    @get:Input
    abstract val versaoModulo: Property<String>

    @get:OutputFile
    abstract val arquivoDestino: RegularFileProperty

    @TaskAction
    fun executar() {
        val arquivo = arquivoDestino.get().asFile
        val conteudo = """
            versao=${versaoModulo.get()}
            dataProcessamento=${java.time.Instant.now()}
            plataformaJdk=25
        """.trimIndent()
        
        arquivo.writeText(conteudo)
    }
}

```

---

## 8. Anti-Patterns em Automação com Gradle a Evitar

* **Uso de `allprojects {}` e `subprojects {}`:** Anti-pattern severo que quebra o isolamento de projeto (*Project Isolation*) e inviabiliza as otimizações modernas de cache. Toda lógica compartilhada deve residir em Convention Plugins (`build-logic`).
* **Resolução Antecipada de Tarefas (`tasks.getByName` ou `tasks.create`):** Força a inicialização e configuração imediata de tarefas que não fazem parte do grafo de execução atual. Use `tasks.named` ou `tasks.register`.
* **Coordenadas de Dependências Hardcoded em Módulos:** Declarar strings de grupos e versões diretamente nos arquivos `build.gradle.kts`, ignorando o `libs.versions.toml`.
* **Acesso ao Objeto `project` em Tempo de Execução:** Usar `project.file(...)` ou `project.name` dentro de `doLast {}` ou no `@TaskAction`, quebrando o Configuration Cache.
* **Execuções sem Gradle Wrapper:** Instruir o desenvolvedor ou pipelines de CI a rodar comandos via binário global `gradle ...` em vez do wrapper `./gradlew ...`.
* **Mistura de Versões de Compilação:** Compilar com a JVM do ambiente do sistema operacional sem travar o Java Toolchain no Java 25 nos plugins de convenção.

---

## 9. Checklist de Auditoria Gradle para o Agente IA

Antes de finalizar qualquer entrega de scripts de compilação ou automação, valide rigorosamente:

1. [ ] **Kotlin DSL Exclusivo:** Todos os scripts utilizam a extensão `.gradle.kts` com tipagem estática pura, sem arquivos Groovy?
2. [ ] **Convention Plugins vs. `subprojects`:** O projeto eliminou os blocos `allprojects` e `subprojects`, orquestrando a lógica compartilhada via `build-logic`?
3. [ ] **Version Catalog Centralizado:** Todas as dependências, plugins e bundles são consumidos do `gradle/libs.versions.toml` sem versões hardcoded?
4. [ ] **Java 25 Toolchain Configurado:** O bloco `java.toolchain` está fixado no Java 25 através da versão declarada no catálogo?
5. [ ] **Conformidade com Configuration Cache:** Todas as tarefas customizadas utilizam Lazy Properties (`Property<T>`, `Provider<T>`) e nenhuma acessa `project` em tempo de execução?
6. [ ] **Evitação de Configuração de Tarefas:** Todas as tarefas foram declaradas usando `tasks.register(...)` e manipuladas com `tasks.named(...)`?
7. [ ] **Otimizações do `gradle.properties`:** O arquivo contém os flags mandatórios ativados (`org.gradle.caching=true`, `org.gradle.configuration-cache=true`, `org.gradle.parallel=true`, `org.gradle.vfs.watch=true`)?