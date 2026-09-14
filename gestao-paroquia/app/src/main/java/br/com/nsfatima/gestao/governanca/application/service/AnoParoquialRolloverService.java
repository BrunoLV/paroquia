package br.com.nsfatima.gestao.governanca.application.service;

import br.com.nsfatima.gestao.governanca.domain.service.SucessaoAnoParoquialService;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquial;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.domain.repository.AnoParoquialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Orquestra a virada entre anos paroquiais: quando um ano é dado como {@code CONCLUIDO} (pelo job
 * agendado de 31/12 ou por ação manual via caso de uso), provisiona automaticamente o próximo ano
 * civil em {@code EM_PLANEJAMENTO} caso ainda não exista. Toda a máquina de estados e a regra de
 * sucessão moram no agregado {@link AnoParoquial} e em {@link SucessaoAnoParoquialService}.
 */
@Service
public class AnoParoquialRolloverService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnoParoquialRolloverService.class);

    private final AnoParoquialRepository repository;
    private final SucessaoAnoParoquialService sucessaoService;

    public AnoParoquialRolloverService(
            AnoParoquialRepository repository,
            SucessaoAnoParoquialService sucessaoService) {
        this.repository = repository;
        this.sucessaoService = sucessaoService;
    }

    /**
     * Rollover automático do Ano Paroquial: executa em 31 de dezembro às 23:59:59.
     */
    @Scheduled(cron = "59 59 23 31 12 *")
    @Transactional
    public void transicionarAnoAutomaticamente() {
        int anoCorrente = LocalDate.now().getYear();
        LOGGER.info("Iniciando rollover automático do Ano Paroquial para o ano {}", anoCorrente);
        concluirAno(anoCorrente);
    }

    /**
     * Conclui um dado ano e cria automaticamente o próximo em EM_PLANEJAMENTO caso não exista.
     */
    @Transactional
    public void concluirAno(int ano) {
        Instant agora = Instant.now();
        AnoParoquial anoParoquial = repository.buscarPorAno(ano)
                .orElseGet(() -> AnoParoquial.abrirPlanejamento(ano, agora));

        if (anoParoquial.getStatus() == AnoParoquialStatus.CONCLUIDO) {
            LOGGER.info("Ano Paroquial {} já está CONCLUIDO.", ano);
            return;
        }

        sucessaoService.assegurarAnosAnterioresConcluidos(ano);
        anoParoquial.concluir(ano, agora);
        repository.salvar(anoParoquial);

        int proximoAno = ano + 1;
        if (repository.buscarPorAno(proximoAno).isEmpty()) {
            repository.salvar(AnoParoquial.abrirPlanejamento(proximoAno, agora));
            LOGGER.info("Rollover concluído: ano {} definido como CONCLUIDO, ano {} inicializado como EM_PLANEJAMENTO",
                    ano, proximoAno);
        } else {
            LOGGER.info("Rollover concluído: ano {} definido como CONCLUIDO. O ano {} já estava inicializado.",
                    ano, proximoAno);
        }
    }
}
