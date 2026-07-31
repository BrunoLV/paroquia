package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.MetricasOciosidadeResponse;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GetMetricasOciosidadeUseCase {

    private final AtivoPatrimonialJpaRepository repository;

    public GetMetricasOciosidadeUseCase(AtivoPatrimonialJpaRepository repository) {
        this.repository = repository;
    }

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
