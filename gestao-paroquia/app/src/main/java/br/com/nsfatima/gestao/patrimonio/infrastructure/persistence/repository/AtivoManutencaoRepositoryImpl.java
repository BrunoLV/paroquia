package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencao;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoManutencaoRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.mapper.AtivoManutencaoMapper;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/** Adaptador de saída que implementa a porta {@link AtivoManutencaoRepository} sobre o Spring Data JPA. */
@Repository
public class AtivoManutencaoRepositoryImpl implements AtivoManutencaoRepository {

    private final AtivoManutencaoJpaRepository jpaRepository;
    private final AtivoManutencaoMapper mapper;

    public AtivoManutencaoRepositoryImpl(AtivoManutencaoJpaRepository jpaRepository,
                                         AtivoManutencaoMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public AtivoManutencao save(AtivoManutencao manutencao) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(manutencao)));
    }

    @Override
    public Optional<AtivoManutencao> findEmAbertoPorAtivo(AtivoPatrimonialId ativoId) {
        return jpaRepository.findFirstByAtivoIdAndDataFimRealIsNull(ativoId.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existeEmAbertoPorAtivo(AtivoPatrimonialId ativoId) {
        return jpaRepository.existsByAtivoIdAndDataFimRealIsNull(ativoId.value());
    }

    @Override
    public List<AtivoManutencao> findPorAtivoOrdenadoPorInicioDesc(AtivoPatrimonialId ativoId) {
        return jpaRepository.findByAtivoIdOrderByDataInicioDesc(ativoId.value())
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AtivoManutencao> findJanelasBloqueantes(AtivoPatrimonialId ativoId, Instant inicio, Instant fim) {
        return jpaRepository.findJanelasBloqueantes(ativoId.value(), inicio, fim)
                .stream().map(mapper::toDomain).toList();
    }
}
