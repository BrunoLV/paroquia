plugins {
    alias(libs.plugins.sonarqube)
}

sonarqube {
    properties {
        property("sonar.projectKey", "gestao-paroquia")
        property("sonar.projectName", "Gestao Paroquia")
        property("sonar.host.url", "http://localhost:9000") // Default local
        property("sonar.token", System.getenv("SONAR_TOKEN") ?: "")
        
        property("sonar.coverage.jacoco.xmlReportPaths", "${project.rootDir}/app/build/reports/jacoco/test/jacocoTestReport.xml")
        property("sonar.java.binaries", "${project.rootDir}/app/build/classes/java/main")
    }
}

tasks.named("sonar") {
    dependsOn(":app:test")
    dependsOn(":app:jacocoTestReport")
}
