package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.CpcDecisaoEntity;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository.CpcDecisaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Consulta atas/decisões de conselho (CPC/CPP), com filtro opcional por comunidade ou por
 * status de homologação. Suporta tanto a listagem geral (auditoria/histórico) quanto a
 * busca de pendências de homologação por um homologador.
 */
@Service
public class ListCpcDecisoesUseCase {

    private final CpcDecisaoJpaRepository repository;

    public ListCpcDecisoesUseCase(CpcDecisaoJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Filtra por comunidade quando informada; senão, filtra por status de homologação quando
     * informado; senão, retorna todas as decisões. {@code comunidadeId} tem precedência sobre
     * {@code status} quando ambos são informados — os dois filtros não são combinados.
     *
     * @param comunidadeId se não nulo, restringe o resultado à comunidade informada
     * @param status se não nulo (e {@code comunidadeId} nulo), restringe pelo status de homologação
     * @return decisões ordenadas por data de reunião (ou por criação, no filtro por status), mais recentes primeiro
     */
    @Transactional(readOnly = true)
    public List<CpcDecisaoResponse> execute(UUID comunidadeId, StatusHomologacao status) {
        List<CpcDecisaoEntity> list;
        if (comunidadeId != null) {
            list = repository.findByComunidadeIdOrderByDataReuniaoDesc(comunidadeId);
        } else if (status != null) {
            list = repository.findByStatusHomologacaoOrderByCriadoEmUtcDesc(status);
        } else {
            list = repository.findAllByOrderByDataReuniaoDesc();
        }

        return list.stream()
                .map(CreateCpcDecisaoUseCase::toResponse)
                .toList();
    }
}
