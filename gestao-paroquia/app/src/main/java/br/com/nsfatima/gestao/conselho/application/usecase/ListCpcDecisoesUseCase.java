package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.CpcDecisaoEntity;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository.CpcDecisaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListCpcDecisoesUseCase {

    private final CpcDecisaoJpaRepository repository;

    public ListCpcDecisoesUseCase(CpcDecisaoJpaRepository repository) {
        this.repository = repository;
    }

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
