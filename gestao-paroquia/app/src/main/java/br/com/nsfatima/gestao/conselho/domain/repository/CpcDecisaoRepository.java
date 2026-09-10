package br.com.nsfatima.gestao.conselho.domain.repository;

import br.com.nsfatima.gestao.conselho.domain.model.CpcDecisao;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência do agregado {@link CpcDecisao}.
 */
public interface CpcDecisaoRepository {

    Optional<CpcDecisao> buscarPorId(UUID id);

    CpcDecisao salvar(CpcDecisao decisao);

    List<CpcDecisao> listarPorComunidade(UUID comunidadeId);

    List<CpcDecisao> listarPorStatus(StatusHomologacao status);

    List<CpcDecisao> listarTodas();
}
