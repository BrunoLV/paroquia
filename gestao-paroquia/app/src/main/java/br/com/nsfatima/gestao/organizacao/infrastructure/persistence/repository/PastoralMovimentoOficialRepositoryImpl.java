package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.PastoralMovimentoOficial;
import br.com.nsfatima.gestao.organizacao.domain.repository.PastoralMovimentoOficialRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.PastoralMovimentoOficialEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador JPA de {@link PastoralMovimentoOficialRepository}, traduzindo entre o modelo de domínio
 * {@code PastoralMovimentoOficial} e a entidade JPA {@link PastoralMovimentoOficialEntity}.
 */
@Repository
public class PastoralMovimentoOficialRepositoryImpl implements PastoralMovimentoOficialRepository {

    private final PastoralMovimentoOficialJpaRepository jpaRepository;

    public PastoralMovimentoOficialRepositoryImpl(PastoralMovimentoOficialJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(PastoralMovimentoOficial domain) {
        PastoralMovimentoOficialEntity entity = new PastoralMovimentoOficialEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setTipo(domain.getTipo());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<PastoralMovimentoOficial> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<PastoralMovimentoOficial> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByNome(String nome) {
        return jpaRepository.existsByNome(nome);
    }

    private PastoralMovimentoOficial toDomain(PastoralMovimentoOficialEntity entity) {
        return new PastoralMovimentoOficial(
                entity.getId(),
                entity.getNome(),
                entity.getTipo()
        );
    }
}
