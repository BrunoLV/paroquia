package br.com.nsfatima.gestao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

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
