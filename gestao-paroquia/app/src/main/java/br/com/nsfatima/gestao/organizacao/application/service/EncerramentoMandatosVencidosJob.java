package br.com.nsfatima.gestao.organizacao.application.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Job diário que encerra os mandatos de coordenação cujo prazo terminou, para que a coordenação seja de fato
 * temporária e a liderança volte a ser submetida à homologação do Pároco.
 */
@Component
public class EncerramentoMandatosVencidosJob {

    private final MandatoCoordenacaoService service;

    public EncerramentoMandatosVencidosJob(MandatoCoordenacaoService service) {
        this.service = service;
    }

    /**
     * Encerra os mandatos vencidos; executa de madrugada (UTC).
     */
    @Scheduled(cron = "0 15 3 * * *", zone = "UTC")
    public void encerrarVencidos() {
        service.encerrarVencidos();
    }
}
