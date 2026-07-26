package br.com.nsfatima.gestao.organizacao.domain.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PastoralMovimentoOficialRepository {
    void save(PastoralMovimentoOficial pastoralOficial);

    Optional<PastoralMovimentoOficial> findById(UUID id);

    List<PastoralMovimentoOficial> findAll();

    void delete(UUID id);

    boolean existsByNome(String nome);
}
