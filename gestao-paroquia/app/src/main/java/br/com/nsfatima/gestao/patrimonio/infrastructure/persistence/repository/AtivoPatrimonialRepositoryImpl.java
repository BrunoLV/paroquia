package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.mapper.AtivoPatrimonialMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Adaptador de saída que implementa a porta {@link AtivoPatrimonialRepository} sobre o Spring Data JPA. */
@Repository
public class AtivoPatrimonialRepositoryImpl implements AtivoPatrimonialRepository {

    private final AtivoPatrimonialJpaRepository jpaRepository;
    private final AtivoPatrimonialMapper mapper;

    public AtivoPatrimonialRepositoryImpl(AtivoPatrimonialJpaRepository jpaRepository,
                                          AtivoPatrimonialMapper mapper) {
        this.jpaRepository = Objects.requireNonNull(jpaRepository);
        this.mapper = Objects.requireNonNull(mapper);
    }

    @Override
    public AtivoPatrimonial save(AtivoPatrimonial ativo) {
        return mapper.toDomain(jpaRepository.save(mapper.toEntity(ativo)));
    }

    @Override
    public Optional<AtivoPatrimonial> findById(AtivoPatrimonialId id) {
        return jpaRepository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<AtivoPatrimonial> findByOrganizacao(UUID organizacaoId) {
        return jpaRepository.findByOrganizacaoIdAndDeletedAtIsNullOrderByNomeAsc(organizacaoId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AtivoPatrimonial> findTodos() {
        return jpaRepository.findByDeletedAtIsNullOrderByNomeAsc()
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public List<AtivoPatrimonial> findSincronizaveisDeReserva() {
        return jpaRepository.findSincronizaveisDeReserva(AtivoStatus.EM_MANUTENCAO)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public long contarPorStatus(AtivoStatus status) {
        return jpaRepository.countByStatusAndDeletedAtIsNull(status);
    }
}
