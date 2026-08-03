package br.com.nsfatima.gestao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Ponto de entrada do monólito modular "Projeto Paróquia". Habilita agendamento
 * ({@code @EnableScheduling}) porque vários bounded contexts dependem de jobs periódicos
 * (rollover de Ano Paroquial, snapshots semanais de métricas, geração de recorrências anuais,
 * expiração de alocações de patrimônio em atraso).
 */
@SpringBootApplication
@EnableScheduling
public class GestaoParoquiaApplication {

    protected GestaoParoquiaApplication() {
    }


    static void main(String... args) {
        preloadLoggingClasses();
        SpringApplication.run(GestaoParoquiaApplication.class, args);
    }

    private static void preloadLoggingClasses() {
        String[] classesToPreload = {
                "ch.qos.logback.classic.spi.ThrowableProxy",
                "ch.qos.logback.classic.spi.LoggingEvent",
                "ch.qos.logback.classic.spi.StackTraceElementProxy"
        };
        for (String className : classesToPreload) {
            try {
                Class.forName(className);
            } catch (ClassNotFoundException ignored) {
            }
        }
    }
}
