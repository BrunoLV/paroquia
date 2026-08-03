package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoAlocacaoResponse;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Lista os ativos patrimoniais alocados a um evento específico, resolvendo em lote os dados dos
 * ativos referenciados para evitar consultas N+1.
 */
@Service
public class ListAlocacoesPorEventoUseCase {

    private final AtivoAlocacaoJpaRepository alocacaoRepository;
    private final AtivoPatrimonialJpaRepository ativoRepository;

    public ListAlocacoesPorEventoUseCase(AtivoAlocacaoJpaRepository alocacaoRepository,
                                          AtivoPatrimonialJpaRepository ativoRepository) {
        this.alocacaoRepository = alocacaoRepository;
        this.ativoRepository = ativoRepository;
    }

    /**
     * Retorna as alocações de ativos vinculadas ao evento, da mais recente para a mais antiga.
     *
     * @param eventoId evento consultado
     * @return alocações de ativos do evento
     */
    @Transactional(readOnly = true)
    public List<AtivoAlocacaoResponse> execute(UUID eventoId) {
        List<AtivoAlocacaoEntity> ocupacoes = alocacaoRepository.findByEventoIdOrderByDataInicioDesc(eventoId);

        List<UUID> ativoIds = ocupacoes.stream().map(AtivoAlocacaoEntity::getAtivoId).distinct().toList();
        Map<UUID, AtivoPatrimonialEntity> ativosPorId = ativoRepository.findAllById(ativoIds).stream()
                .collect(java.util.stream.Collectors.toMap(AtivoPatrimonialEntity::getId, Function.identity()));

        return ocupacoes.stream()
                .map(ocupacao -> toResponse(ocupacao, ativosPorId.get(ocupacao.getAtivoId())))
                .toList();
    }

    private AtivoAlocacaoResponse toResponse(AtivoAlocacaoEntity ocupacao, AtivoPatrimonialEntity ativo) {
        String ativoNome = ativo != null ? ativo.getNome() : null;
        String numeroTombamento = ativo != null ? ativo.getNumeroTombamento() : null;
        return new AtivoAlocacaoResponse(
                ocupacao.getId(), ocupacao.getAtivoId(), ativoNome, numeroTombamento, ocupacao.getTipo(),
                ocupacao.getEventoId(), ocupacao.getOrganizacaoOrigemId(), ocupacao.getOrganizacaoDestinoId(),
                ocupacao.isEmprestimo(), ocupacao.getDataInicio(), ocupacao.getDataFim(), ocupacao.getStatus(),
                ocupacao.isImpactadaPorAtraso(), ocupacao.getCriadoEmUtc(), ocupacao.getDevolvidoEmUtc());
    }
}
