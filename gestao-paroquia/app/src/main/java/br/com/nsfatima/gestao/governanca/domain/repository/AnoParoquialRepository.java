package br.com.nsfatima.gestao.governanca.domain.repository;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquial;

import java.util.Optional;

/**
 * Porta de persistência do agregado {@link AnoParoquial}.
 */
public interface AnoParoquialRepository {

    Optional<AnoParoquial> buscarPorAno(int ano);

    AnoParoquial salvar(AnoParoquial anoParoquial);

    /**
     * Regra de sucessão: indica se existe algum ano estritamente anterior a {@code ano}
     * que ainda não esteja {@code CONCLUIDO}.
     */
    boolean existeAnoAnteriorNaoConcluido(int ano);
}
