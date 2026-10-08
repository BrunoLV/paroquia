package br.com.nsfatima.gestao.local.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.domain.model.EnderecoLocal;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.repository.LocalRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adapta a porta de domínio {@link LocalRepository} para a infraestrutura JPA, convertendo entre o
 * agregado de domínio {@link Local} e a entidade {@link LocalEntity}, e resolvendo dependências
 * cruzadas de outros contextos (Evento, Organização) apenas através de seus repositórios JPA — nunca
 * expondo as entidades desses contextos ao domínio de {@code local}.
 */
@Repository
public class LocalRepositoryImpl implements LocalRepository {

    private final LocalJpaRepository localJpaRepository;
    private final EventoJpaRepository eventoJpaRepository;
    private final OrganizacaoJpaRepository organizacaoJpaRepository;
    private final UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository;

    /**
     * Inicializa o adaptador com os repositórios JPA necessários para persistir Locais e resolver
     * seus vínculos com Eventos (verificação de uso) e Organizações (nome para exibição).
     *
     * @param localJpaRepository            repositório JPA de Locais
     * @param eventoJpaRepository           repositório JPA de Eventos, usado para checar uso de um local
     * @param organizacaoJpaRepository   repositório JPA de Grupos/Pastorais
     * @param unidadeTerritorialJpaRepository repositório JPA de Matriz/Comunidades vinculadas
     */
    public LocalRepositoryImpl(LocalJpaRepository localJpaRepository,
                                EventoJpaRepository eventoJpaRepository,
                                OrganizacaoJpaRepository organizacaoJpaRepository,
                                UnidadeTerritorialJpaRepository unidadeTerritorialJpaRepository) {
        this.localJpaRepository = localJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.organizacaoJpaRepository = organizacaoJpaRepository;
        this.unidadeTerritorialJpaRepository = unidadeTerritorialJpaRepository;
    }

    @Override
    public void save(Local local) {
        LocalEntity entity = toEntity(local);
        localJpaRepository.save(entity);
    }

    @Override
    public Optional<Local> findById(UUID id) {
        return localJpaRepository.findByIdAndDeletedAtIsNull(id).map(this::toDomain);
    }

    @Override
    public List<Local> findAll() {
        return localJpaRepository.findAllByDeletedAtIsNull().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Local> findByOrganizacaoId(UUID organizacaoId) {
        return localJpaRepository.findByOrganizacaoIdAndDeletedAtIsNull(organizacaoId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isLocalInUse(UUID id) {
        return eventoJpaRepository.existsByLocalId(id);
    }

    @Override
    public boolean hasFutureEvents(UUID id) {
        return eventoJpaRepository.existsByLocalIdAndFimUtcGreaterThanAndStatusNot(id, Instant.now(), "CANCELADO");
    }

    @Override
    public void delete(UUID id) {
        localJpaRepository.deleteById(id);
    }

    private LocalEntity toEntity(Local domain) {
        LocalEntity entity = new LocalEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setTipo(domain.getTipo());
        entity.setCapacidadeMaxima(domain.getCapacidadeMaxima());
        entity.setCapacidade(domain.getCapacidadeMaxima());
        entity.setDescricao(domain.getDescricao());
        entity.setCaracteristicas(domain.getDescricao());
        entity.setAtivo(domain.isAtivo());
        entity.setOrganizacaoId(domain.getIgrejaId());
        entity.setDeletedAt(domain.getDeletedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setUpdatedAt(domain.getUpdatedAt());

        EnderecoLocal el = domain.getEndereco();
        if (el != null) {
            entity.setCep(el.cep());
            entity.setLogradouro(el.logradouro());
            entity.setNumero(el.numero());
            entity.setComplemento(el.complemento());
            entity.setBairro(el.bairro());
            entity.setCidade(el.cidade());
            entity.setUf(el.uf());
            entity.setEndereco(el.formatado());
        }
        return entity;
    }

    private Local toDomain(LocalEntity entity) {
        String igrejaNome = null;
        if (entity.getOrganizacaoId() != null) {
            igrejaNome = unidadeTerritorialJpaRepository.findById(entity.getOrganizacaoId())
                    .map(UnidadeTerritorialEntity::getNome)
                    .or(() -> organizacaoJpaRepository.findById(entity.getOrganizacaoId())
                            .map(OrganizacaoEntity::getNome))
                    .orElse(null);
        }

        EnderecoLocal enderecoLocal = new EnderecoLocal(
                entity.getCep(),
                entity.getLogradouro() != null ? entity.getLogradouro() : entity.getEndereco(),
                entity.getNumero(),
                entity.getComplemento(),
                entity.getBairro(),
                entity.getCidade(),
                entity.getUf()
        );

        Integer capacidadeMaxima = entity.getCapacidadeMaxima() != null
                ? entity.getCapacidadeMaxima()
                : entity.getCapacidade();

        String descricao = entity.getDescricao() != null
                ? entity.getDescricao()
                : entity.getCaracteristicas();

        return new Local(
                entity.getId(),
                entity.getNome(),
                entity.getTipo(),
                enderecoLocal,
                capacidadeMaxima,
                descricao,
                entity.isAtivo(),
                entity.getOrganizacaoId(),
                igrejaNome,
                entity.getDeletedAt(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
