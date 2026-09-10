package br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquial;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.domain.repository.AnoParoquialRepository;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.mapper.AnoParoquialMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador da porta {@link AnoParoquialRepository} sobre o {@link AnoParoquialJpaRepository}.
 */
@Repository
public class AnoParoquialRepositoryImpl implements AnoParoquialRepository {

    private final AnoParoquialJpaRepository jpaRepository;
    private final AnoParoquialMapper mapper;

    public AnoParoquialRepositoryImpl(AnoParoquialJpaRepository jpaRepository, AnoParoquialMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<AnoParoquial> buscarPorAno(int ano) {
        return jpaRepository.findByAno(ano).map(mapper::toDomain);
    }

    @Override
    public AnoParoquial salvar(AnoParoquial anoParoquial) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(anoParoquial)));
    }

    @Override
    public boolean existeAnoAnteriorNaoConcluido(int ano) {
        return jpaRepository.existsByAnoLessThanAndStatusNot(ano, AnoParoquialStatus.CONCLUIDO);
    }
}
