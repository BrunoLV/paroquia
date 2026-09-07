package br.com.nsfatima.gestao.fiel.infrastructure.scheduling;

import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarCicloVidaFielUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Executa diariamente a retenção de cinco anos, sem registrar dados pessoais em log. */
@Component
public class AnonimizacaoCadastroFielScheduler {

    private final GerenciarCicloVidaFielUseCase cicloVidaFielUseCase;

    public AnonimizacaoCadastroFielScheduler(GerenciarCicloVidaFielUseCase cicloVidaFielUseCase) {
        this.cicloVidaFielUseCase = cicloVidaFielUseCase;
    }

    @Scheduled(cron = "0 10 0 * * *", zone = "UTC")
    public void executar() {
        cicloVidaFielUseCase.anonimizarCadastrosVencidos();
    }
}
