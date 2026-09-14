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
        SpringApplication.run(GestaoParoquiaApplication.class, args);
    }
}
