package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.TaxaConclusaoProjetosResponse;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CalcularTaxaProjetosUseCase {

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

        double taxaConclusao = total > 0 ? (concluidos * 100.0) / total : 100.0;
        double taxaAbandono = total > 0 ? (cancelados * 100.0) / total : 0.0;

        String avaliacao;
        if (taxaAbandono >= 30.0) {
            avaliacao = "CRITICO";
        } else if (taxaAbandono >= 15.0) {
            avaliacao = "ALERTA";
        } else {
            avaliacao = "SAUDAVEL";
        }

        return new TaxaConclusaoProjetosResponse(
                total,
                concluidos,
                emAndamento,
                cancelados,
                Math.round(taxaConclusao * 10.0) / 10.0,
                Math.round(taxaAbandono * 10.0) / 10.0,
                avaliacao
        );
    }

    private boolean isStatus(ProjetoEventoEntity projeto, ProjetoStatus targetStatus) {
        if (projeto.getStatus() == null) {
            return false;
        }
        return targetStatus.name().equalsIgnoreCase(projeto.getStatus());
    }
}
