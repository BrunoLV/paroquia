package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramentalId;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity.AssentoSacramentalEntity;
import br.com.nsfatima.gestao.sacramento.infrastructure.persistence.mapper.AssentoSacramentalMapper;
import org.springframework.stereotype.Repository;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** Adaptador de saída da porta {@link AssentoSacramentalRepository} sobre Spring Data JPA. */
@Repository
public class AssentoSacramentalRepositoryImpl implements AssentoSacramentalRepository {

    private final AssentoSacramentalJpaRepository jpa;
    private final AssentoSacramentalMapper mapper;

    public AssentoSacramentalRepositoryImpl(AssentoSacramentalJpaRepository jpa, AssentoSacramentalMapper mapper) {
        this.jpa = jpa;
        this.mapper = mapper;
    }

    @Override
    public AssentoSacramental save(AssentoSacramental assento) {
        AssentoSacramentalEntity entity = jpa.findById(assento.getId().value())
                .orElseGet(() -> new AssentoSacramentalEntity(assento.getId().value()));
        mapper.aplicar(entity, assento);
        return mapper.toDomain(jpa.saveAndFlush(entity));
    }

    @Override
    public Optional<AssentoSacramental> findById(AssentoSacramentalId id) {
        return jpa.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public List<AssentoSacramental> listar(TipoSacramento tipo, boolean incluirReservados) {
        return jpa.listar(tipo, visiveis(incluirReservados)).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existeReferencia(TipoSacramento tipo, String livro, String folha, String termo, UUID ignorarId) {
        return jpa.existeReferencia(tipo, livro, folha, termo, ignorarId);
    }

    @Override
    public List<AssentoSacramental> buscarPorVinculo(UUID pessoaId, boolean incluirReservados) {
        return jpa.buscarPorVinculo(pessoaId, visiveis(incluirReservados)).stream().map(mapper::toDomain).toList();
    }

    private static Set<Visibilidade> visiveis(boolean incluirReservados) {
        return incluirReservados ? EnumSet.allOf(Visibilidade.class) : EnumSet.of(Visibilidade.COMUM);
    }
}
