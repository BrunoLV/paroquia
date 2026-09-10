package br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.conselho.domain.model.CpcDecisao;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.domain.repository.CpcDecisaoRepository;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.CpcDecisaoEntity;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.mapper.CpcDecisaoMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adaptador da porta {@link CpcDecisaoRepository} sobre o {@link CpcDecisaoJpaRepository}.
 */
@Repository
public class CpcDecisaoRepositoryImpl implements CpcDecisaoRepository {

    private final CpcDecisaoJpaRepository jpaRepository;
    private final CpcDecisaoMapper mapper;

    public CpcDecisaoRepositoryImpl(CpcDecisaoJpaRepository jpaRepository, CpcDecisaoMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<CpcDecisao> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public CpcDecisao salvar(CpcDecisao decisao) {
        CpcDecisaoEntity entity = jpaRepository.findById(decisao.getId())
                .map(existente -> {
                    mapper.aplicar(decisao, existente);
                    return existente;
                })
                .orElseGet(() -> mapper.toEntity(decisao));
        return mapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public List<CpcDecisao> listarPorComunidade(UUID comunidadeId) {
        return jpaRepository.findByComunidadeIdOrderByDataReuniaoDesc(comunidadeId).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<CpcDecisao> listarPorStatus(StatusHomologacao status) {
        return jpaRepository.findByStatusHomologacaoOrderByCriadoEmUtcDesc(status).stream()
                .map(mapper::toDomain).toList();
    }

    @Override
    public List<CpcDecisao> listarTodas() {
        return jpaRepository.findAllByOrderByDataReuniaoDesc().stream()
                .map(mapper::toDomain).toList();
    }
}
