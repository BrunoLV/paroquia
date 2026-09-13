package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.TaxaConclusaoProjetosResponse;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calcula a taxa de conversão e realização de projetos e eventos planejados.
 */
@Service
public class CalcularTaxaProjetosUseCase {

    private static final int PERCENTUAL_SCALE = 1;
    private static final BigDecimal LIMIAR_CRITICO = BigDecimal.valueOf(30);
    private static final BigDecimal LIMIAR_ALERTA = BigDecimal.valueOf(15);

    private final ProjetoEventoJpaRepository projetoEventoJpaRepository;

    public CalcularTaxaProjetosUseCase(ProjetoEventoJpaRepository projetoEventoJpaRepository) {
        this.projetoEventoJpaRepository = projetoEventoJpaRepository;
    }

    /**
     * Calcula a Taxa de Conclusão e Abandono/Cancelamento de Projetos aprovados pelos conselhos CPP e CAE.
     *
     * @return DTO estatístico com contagens, percentuais e avaliação executiva
     */
    @Transactional(readOnly = true)
    public TaxaConclusaoProjetosResponse execute() {
        List<ProjetoEventoEntity> projetos = projetoEventoJpaRepository.findAll();

        long total = projetos.size();
        long concluidos = projetos.stream()
                .filter(p -> isStatus(p, ProjetoStatus.CONCLUIDO))
                .count();
        long emAndamento = projetos.stream()
                .filter(p -> isStatus(p, ProjetoStatus.EM_ANDAMENTO) || isStatus(p, ProjetoStatus.ABERTO))
                .count();
        long cancelados = projetos.stream()
                .filter(p -> isStatus(p, ProjetoStatus.CANCELADO))
                .count();

        BigDecimal taxaConclusao = total > 0
                ? calcularPercentual(concluidos, total)
                : BigDecimal.valueOf(100).setScale(PERCENTUAL_SCALE, RoundingMode.HALF_UP);
        BigDecimal taxaAbandono = total > 0 ? calcularPercentual(cancelados, total) : BigDecimal.ZERO;

        String avaliacao;
        if (taxaAbandono.compareTo(LIMIAR_CRITICO) >= 0) {
            avaliacao = "CRITICO";
        } else if (taxaAbandono.compareTo(LIMIAR_ALERTA) >= 0) {
            avaliacao = "ALERTA";
        } else {
            avaliacao = "SAUDAVEL";
        }

        return new TaxaConclusaoProjetosResponse(
                total,
                concluidos,
                emAndamento,
                cancelados,
                taxaConclusao,
                taxaAbandono,
                avaliacao
        );
    }

    /** Percentual numerador/denominador com precisão decimal exata; {@code ZERO} se não houver denominador. */
    private BigDecimal calcularPercentual(long numerador, long denominador) {
        return denominador == 0L
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(numerador)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(denominador), PERCENTUAL_SCALE, RoundingMode.HALF_UP);
    }

    private boolean isStatus(ProjetoEventoEntity projeto, ProjetoStatus targetStatus) {
        return targetStatus == projeto.getStatus();
    }
}
