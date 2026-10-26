package br.com.nsfatima.gestao.clero.infrastructure.scheduling;

import br.com.nsfatima.gestao.clero.application.usecase.GerenciarCicloVidaClerigoUseCase;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Executa diariamente a retenção de cinco anos dos clérigos inativos, sem registrar dados pessoais em log. */
@Component
public class AgendadorAnonimizacaoCadastroClerigoJob {

    private final GerenciarCicloVidaClerigoUseCase cicloVidaClerigoUseCase;

    public AgendadorAnonimizacaoCadastroClerigoJob(GerenciarCicloVidaClerigoUseCase cicloVidaClerigoUseCase) {
        this.cicloVidaClerigoUseCase = cicloVidaClerigoUseCase;
    }

    @Scheduled(cron = "0 20 0 * * *", zone = "UTC")
    public void executar() {
        cicloVidaClerigoUseCase.anonimizarCadastrosVencidos();
    }
}
