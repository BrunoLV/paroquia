package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPeriodoConflitanteException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AtivoAlocacaoConflitoPolicy {

    private static final List<AtivoAlocacaoStatus> STATUS_OCUPANTES = List.of(
            AtivoAlocacaoStatus.PENDENTE_APROVACAO,
            AtivoAlocacaoStatus.CONFIRMADA,
            AtivoAlocacaoStatus.ATRASADA);

    private final AtivoAlocacaoJpaRepository alocacaoRepository;

    public AtivoAlocacaoConflitoPolicy(AtivoAlocacaoJpaRepository alocacaoRepository) {
        this.alocacaoRepository = alocacaoRepository;
    }

    public void assertPeriodoDisponivel(UUID ativoId, LocalDate dataInicio, LocalDate dataFim) {
        assertPeriodoDisponivel(ativoId, dataInicio, dataFim, null);
    }

    public void assertPeriodoDisponivel(UUID ativoId, LocalDate dataInicio, LocalDate dataFim,
                                         UUID ignorarAlocacaoId) {
        List<AtivoAlocacaoEntity> ocupacoes = alocacaoRepository.findByAtivoIdAndStatusIn(ativoId, STATUS_OCUPANTES);

        boolean conflita = ocupacoes.stream()
                .filter(ocupacao -> !ocupacao.getId().equals(ignorarAlocacaoId))
                .anyMatch(ocupacao ->
                        !ocupacao.getDataInicio().isAfter(dataFim) && !dataInicio.isAfter(ocupacao.getDataFim()));

        if (conflita) {
            throw new AtivoPeriodoConflitanteException(
                    "O ativo já possui uma ocupação confirmada ou pendente nesse período");
        }
    }
}
