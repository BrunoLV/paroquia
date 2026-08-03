package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoAlocacaoResponse;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Lista o histórico completo de ocupações (alocações a eventos e períodos de manutenção) de um
 * ativo patrimonial, ordenado da mais recente para a mais antiga.
 */
@Service
public class ListAlocacoesPorAtivoUseCase {

    private final AtivoAlocacaoJpaRepository alocacaoRepository;
    private final AtivoPatrimonialJpaRepository ativoRepository;

    public ListAlocacoesPorAtivoUseCase(AtivoAlocacaoJpaRepository alocacaoRepository,
                                         AtivoPatrimonialJpaRepository ativoRepository) {
        this.alocacaoRepository = alocacaoRepository;
        this.ativoRepository = ativoRepository;
    }

    /**
     * Retorna todas as ocupações registradas para o ativo, da mais recente para a mais antiga.
     *
     * @param ativoId ativo consultado
     * @return histórico de ocupações do ativo
     * @throws AtivoPatrimonialNotFoundException se o ativo não existir
     */
    @Transactional(readOnly = true)
    public List<AtivoAlocacaoResponse> execute(UUID ativoId) {
        AtivoPatrimonialEntity ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new AtivoPatrimonialNotFoundException(
                        "Ativo patrimonial não encontrado: " + ativoId));

        return alocacaoRepository.findByAtivoIdOrderByDataInicioDesc(ativoId).stream()
                .map(ocupacao -> toResponse(ocupacao, ativo))
                .toList();
    }

    private AtivoAlocacaoResponse toResponse(AtivoAlocacaoEntity ocupacao, AtivoPatrimonialEntity ativo) {
        return new AtivoAlocacaoResponse(
                ocupacao.getId(), ativo.getId(), ativo.getNome(), ativo.getNumeroTombamento(), ocupacao.getTipo(),
                ocupacao.getEventoId(), ocupacao.getOrganizacaoOrigemId(), ocupacao.getOrganizacaoDestinoId(),
                ocupacao.isEmprestimo(), ocupacao.getDataInicio(), ocupacao.getDataFim(), ocupacao.getStatus(),
                ocupacao.isImpactadaPorAtraso(), ocupacao.getCriadoEmUtc(), ocupacao.getDevolvidoEmUtc());
    }
}
