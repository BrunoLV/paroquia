package br.com.nsfatima.gestao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GestaoParoquiaApplication {
    public static void main(String[] args) {
        SpringApplication.run(GestaoParoquiaApplication.class, args);
    }
}
