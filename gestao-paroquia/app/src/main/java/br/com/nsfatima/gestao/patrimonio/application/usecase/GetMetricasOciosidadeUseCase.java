package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.MetricasOciosidadeResponse;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Métricas de ociosidade do acervo: contagem de bens não excluídos por status e taxa de
 * ociosidade (proporção de {@code DISPONIVEL} sobre o total incorporado).
 */
@Service
public class GetMetricasOciosidadeUseCase {

    private static final int TAXA_SCALE = 4;

    private final AtivoPatrimonialRepository repository;

    public GetMetricasOciosidadeUseCase(AtivoPatrimonialRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public MetricasOciosidadeResponse execute() {
        long disponiveis = repository.contarPorStatus(AtivoStatus.DISPONIVEL);
        long reservados = repository.contarPorStatus(AtivoStatus.RESERVADO);
        long emManutencao = repository.contarPorStatus(AtivoStatus.EM_MANUTENCAO);
        long total = disponiveis + reservados + emManutencao;
        BigDecimal taxaOciosidade = calcularTaxa(disponiveis, total);
        return new MetricasOciosidadeResponse(total, disponiveis, reservados, emManutencao, taxaOciosidade);
    }

    /** Quociente numerador/denominador com precisão decimal exata; {@code ZERO} se não houver denominador. */
    private BigDecimal calcularTaxa(long numerador, long denominador) {
        return denominador == 0L
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(numerador)
                        .divide(BigDecimal.valueOf(denominador), TAXA_SCALE, RoundingMode.HALF_UP);
    }
}
