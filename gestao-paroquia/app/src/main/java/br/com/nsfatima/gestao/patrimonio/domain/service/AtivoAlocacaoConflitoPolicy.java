package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPeriodoConflitanteException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Garante que um novo período de ocupação (alocação a evento ou manutenção) de um ativo
 * patrimonial não se sobreponha a outra ocupação já registrada. Considera como ocupantes tanto as
 * ocupações confirmadas/atrasadas quanto as ainda pendentes de aprovação de empréstimo, já que
 * estas também reservam o período mesmo antes de decididas.
 */
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

    /**
     * Valida que o período informado está livre para o ativo, sem excluir nenhuma ocupação
     * existente da checagem.
     *
     * @param ativoId   ativo verificado
     * @param dataInicio início do novo período
     * @param dataFim   fim do novo período
     * @throws AtivoPeriodoConflitanteException se o período conflitar com outra ocupação
     */
    public void assertPeriodoDisponivel(UUID ativoId, LocalDate dataInicio, LocalDate dataFim) {
        assertPeriodoDisponivel(ativoId, dataInicio, dataFim, null);
    }

    /**
     * Valida que o período informado está livre para o ativo, ignorando a própria ocupação em
     * ajuste (usado ao reconfirmar uma ocupação já existente, para não conflitar consigo mesma).
     *
     * @param ativoId          ativo verificado
     * @param dataInicio       início do novo período
     * @param dataFim          fim do novo período
     * @param ignorarAlocacaoId ocupação a excluir da checagem de conflito, ou {@code null}
     * @throws AtivoPeriodoConflitanteException se o período conflitar com outra ocupação
     */
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
