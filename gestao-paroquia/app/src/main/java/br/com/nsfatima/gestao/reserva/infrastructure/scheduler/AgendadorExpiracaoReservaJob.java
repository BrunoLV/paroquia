package br.com.nsfatima.gestao.reserva.infrastructure.scheduler;

import br.com.nsfatima.gestao.reserva.application.usecase.ExpirarReservasPendentesUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Agendador periódico para varredura e expiração preventiva de reservas pendentes (TTL 48h).
 */
@Component
public class AgendadorExpiracaoReservaJob {

    private static final Logger log = LoggerFactory.getLogger(AgendadorExpiracaoReservaJob.class);

    private final ExpirarReservasPendentesUseCase useCase;

    public AgendadorExpiracaoReservaJob(ExpirarReservasPendentesUseCase useCase) {
        this.useCase = useCase;
    }

    @Scheduled(cron = "0 */15 * * * *")
    public void processarExpiracoes() {
        try {
            int expiradas = useCase.executar();
            if (expiradas > 0) {
                log.info("Processamento de TTL concluído: {} reservas pendentes foram expiradas.", expiradas);
            }
        } catch (Exception e) {
            log.error("Erro durante o processamento de expiração de reservas pendentes: {}", e.getMessage(), e);
        }
    }
}
