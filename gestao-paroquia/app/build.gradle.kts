plugins {
    id("java")
    id("jacoco")
    id("checkstyle")
    id("pmd")
    alias(libs.plugins.spotbugs)
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.flyway)
}

group = "br.com.nsfatima"
version = "0.3.0"


checkstyle {
    toolVersion = libs.versions.checkstyle.tool.get()
    configFile = file("${rootDir}/config/checkstyle/checkstyle.xml")
    isIgnoreFailures = false
}

pmd {
    toolVersion = libs.versions.pmd.tool.get()
    isConsoleOutput = true
    isIgnoreFailures = false
    ruleSetFiles = files("${rootDir}/config/pmd/pmd.xml")
    ruleSets = listOf()
}

spotbugs {
    ignoreFailures.set(true)
    excludeFilter.set(file("${rootDir}/config/spotbugs/spotbugs-filter.xml"))
}

repositories {
    mavenCentral()
}

configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
}

dependencies {
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.jetty)
    implementation(libs.spring.boot.starter.log4j2)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.cache)
    implementation(libs.caffeine)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.registry.prometheus)
    implementation(libs.springdoc.openapi)
    implementation(libs.spring.boot.starter.flyway)
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation(libs.flyway.database.postgresql)
    implementation(libs.postgresql)
    implementation(libs.apache.poi)
    implementation(libs.openpdf)

    testImplementation(libs.junit.jupiter)
    testImplementation(libs.archunit.junit5)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.starter.webmvc.test)
    testImplementation(libs.spring.boot.starter.data.jpa.test)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)

    testRuntimeOnly(libs.junit.platform.launcher)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports {
        create("html") {
            required.set(true)
        }
    }
}

tasks.withType<Pmd> {
    classpath = files()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:deprecation")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    maxHeapSize = "2048m"
    finalizedBy("jacocoTestReport")
}

tasks.named<Jar>("jar") {
    enabled = false
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    archiveFileName.set("app.jar")
}

tasks.withType<JacocoReport> {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}

tasks.named<JacocoCoverageVerification>("jacocoTestCoverageVerification") {
    dependsOn("jacocoTestReport")
    violationRules {
        rule {
            limit {
                counter = "INSTRUCTION"
                value = "COVEREDRATIO"
                // Baseline: 83.5% medido em 2026-08-03 — threshold 80% para headroom seguro
                minimum = "0.80".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                // Baseline: 59.5% medido em 2026-08-03 — threshold 55% para headroom seguro
                minimum = "0.55".toBigDecimal()
            }
        }
    }
}

tasks.named("check") {
    dependsOn("jacocoTestCoverageVerification")
}
