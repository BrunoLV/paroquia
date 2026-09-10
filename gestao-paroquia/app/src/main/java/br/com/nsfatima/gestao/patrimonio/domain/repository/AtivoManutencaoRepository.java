package br.com.nsfatima.gestao.patrimonio.domain.repository;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencao;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Porta de saída (Driven Port) para persistência e consulta do agregado {@link AtivoManutencao}.
 */
public interface AtivoManutencaoRepository {

    AtivoManutencao save(AtivoManutencao manutencao);

    /** Manutenção em aberto ({@code data_fim_real IS NULL}) do ativo, se houver. */
    Optional<AtivoManutencao> findEmAbertoPorAtivo(AtivoPatrimonialId ativoId);

    boolean existeEmAbertoPorAtivo(AtivoPatrimonialId ativoId);

    /** Histórico completo de manutenções do ativo, da mais recente para a mais antiga. */
    List<AtivoManutencao> findPorAtivoOrdenadoPorInicioDesc(AtivoPatrimonialId ativoId);

    /**
     * Manutenções do ativo em aberto ou com período sobrepondo o intervalo — janelas que bloqueiam
     * o calendário de reservas.
     */
    List<AtivoManutencao> findJanelasBloqueantes(AtivoPatrimonialId ativoId, Instant inicio, Instant fim);
}
