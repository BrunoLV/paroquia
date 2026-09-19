package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.domain.model.CpcDecisao;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.domain.repository.CpcDecisaoRepository;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.mapper.CpcDecisaoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Consulta atas/decisões de conselho (CPC/CPP), com filtro opcional por comunidade ou por
 * status de homologação. {@code comunidadeId} tem precedência sobre {@code status} — os dois
 * filtros não são combinados.
 */
@Service
public class ListarCpcDecisoesUseCase {

    private final CpcDecisaoRepository repository;
    private final CpcDecisaoMapper mapper;

    public ListarCpcDecisoesUseCase(CpcDecisaoRepository repository, CpcDecisaoMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CpcDecisaoResponse> execute(UUID comunidadeId, StatusHomologacao status) {
        List<CpcDecisao> decisoes;
        if (comunidadeId != null) {
            decisoes = repository.listarPorComunidade(comunidadeId);
        } else if (status != null) {
            decisoes = repository.listarPorStatus(status);
        } else {
            decisoes = repository.listarTodas();
        }
        return decisoes.stream().map(mapper::toResponse).toList();
    }
}
