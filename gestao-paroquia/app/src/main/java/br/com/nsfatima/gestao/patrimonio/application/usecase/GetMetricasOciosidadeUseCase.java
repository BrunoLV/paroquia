package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.MetricasOciosidadeResponse;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Calcula métricas agregadas de ociosidade do acervo patrimonial (contagem por status e taxa de
 * ociosidade global), usadas para identificar bens subutilizados e apoiar decisões de
 * realocação/baixa.
 */
@Service
public class GetMetricasOciosidadeUseCase {

    private final AtivoPatrimonialJpaRepository repository;

    public GetMetricasOciosidadeUseCase(AtivoPatrimonialJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Agrega as contagens de ativos por status e a distribuição por categoria dos ativos
     * disponíveis (ociosos), calculando a taxa de ociosidade como a proporção de ativos
     * disponíveis sobre o total do acervo.
     *
     * @return métricas agregadas de ociosidade/alocação
     */
    public MetricasOciosidadeResponse execute() {
        long total = repository.count();
        long disponiveis = repository.countByStatus(AtivoStatus.DISPONIVEL);
        long alocados = repository.countByStatus(AtivoStatus.ALOCADO);
        long emManutencao = repository.countByStatus(AtivoStatus.EM_MANUTENCAO);
        long baixados = repository.countByStatus(AtivoStatus.BAIXADO);
        double taxaOciosidade = total == 0 ? 0.0 : (double) disponiveis / total;

        Map<String, Long> distribuicaoPorCategoria = repository.findByStatus(AtivoStatus.DISPONIVEL).stream()
                .collect(Collectors.groupingBy(ativo -> ativo.getCategoria().name(), Collectors.counting()));

        return new MetricasOciosidadeResponse(total, disponiveis, alocados, emManutencao, baixados,
                taxaOciosidade, distribuicaoPorCategoria);
    }
}
