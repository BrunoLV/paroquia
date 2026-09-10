package br.com.nsfatima.gestao.governanca.application.usecase;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquial;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.domain.repository.AnoParoquialRepository;
import br.com.nsfatima.gestao.governanca.application.service.AnoParoquialRolloverService;
import br.com.nsfatima.gestao.governanca.domain.service.SucessaoAnoParoquialService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Aplica uma transição de status ao Ano Paroquial. Toda a máquina de estados e as invariantes
 * moram no agregado {@link AnoParoquial} e em {@link SucessaoAnoParoquialService}; este caso de
 * uso apenas carrega o agregado, dispara o comando de negócio e persiste.
 */
@Service
public class AlterarStatusAnoParoquialUseCase {

    private final AnoParoquialRepository repository;
    private final SucessaoAnoParoquialService sucessaoService;
    private final AnoParoquialRolloverService rolloverService;

    public AlterarStatusAnoParoquialUseCase(
            AnoParoquialRepository repository,
            SucessaoAnoParoquialService sucessaoService,
            AnoParoquialRolloverService rolloverService) {
        this.repository = repository;
        this.sucessaoService = sucessaoService;
        this.rolloverService = rolloverService;
    }

    @Transactional
    public AnoParoquial alterarStatus(int ano, AnoParoquialStatus alvo) {
        Instant agora = Instant.now();
        int anoCivilCorrente = LocalDate.now().getYear();
        AnoParoquial anoParoquial = repository.buscarPorAno(ano)
                .orElseGet(() -> AnoParoquial.abrirPlanejamento(ano, agora));

        switch (alvo) {
            case EM_PLANEJAMENTO -> anoParoquial.reabrirPlanejamento(agora);
            case FECHADO -> {
                sucessaoService.assegurarAnosAnterioresConcluidos(ano);
                anoParoquial.fechar(anoCivilCorrente, agora);
            }
            case CONCLUIDO -> {
                anoParoquial.concluir(anoCivilCorrente, agora);
                rolloverService.concludeYear(ano);
                return repository.buscarPorAno(ano).orElseThrow();
            }
            default -> throw new IllegalArgumentException("Status de ano paroquial não suportado: " + alvo);
        }

        return repository.salvar(anoParoquial);
    }
}
