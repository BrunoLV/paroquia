package br.com.nsfatima.gestao.local.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.local.domain.model.Local;
import br.com.nsfatima.gestao.local.domain.repository.LocalRepository;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Repository;

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

    /**
     * Inicializa o adaptador com os repositórios JPA necessários para persistir Locais e resolver
     * seus vínculos com Eventos (verificação de uso) e Organizações (nome para exibição).
     *
     * @param localJpaRepository        repositório JPA de Locais
     * @param eventoJpaRepository       repositório JPA de Eventos, usado para checar uso de um local
     * @param organizacaoJpaRepository  repositório JPA de Organizações, usado para resolver o nome
     *                                  da organização vinculada a um local
     */
    public LocalRepositoryImpl(LocalJpaRepository localJpaRepository,
                                EventoJpaRepository eventoJpaRepository,
                                OrganizacaoJpaRepository organizacaoJpaRepository) {
        this.localJpaRepository = localJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.organizacaoJpaRepository = organizacaoJpaRepository;
    }

    @Override
    public void save(Local local) {
        LocalEntity entity = toEntity(local);
        localJpaRepository.save(entity);
    }

    @Override
    public Optional<Local> findById(UUID id) {
        return localJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Local> findAll() {
        return localJpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Local> findByOrganizacaoId(UUID organizacaoId) {
        return localJpaRepository.findByOrganizacaoId(organizacaoId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isLocalInUse(UUID id) {
        return eventoJpaRepository.existsByLocalId(id);
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
        entity.setEndereco(domain.getEndereco());
        entity.setCapacidade(domain.getCapacidade());
        entity.setCaracteristicas(domain.getCaracteristicas());
        entity.setAtivo(domain.isAtivo());
        entity.setOrganizacaoId(domain.getOrganizacaoId());
        return entity;
    }

    private Local toDomain(LocalEntity entity) {
        String organizacaoNome = null;
        if (entity.getOrganizacaoId() != null) {
            organizacaoNome = organizacaoJpaRepository.findById(entity.getOrganizacaoId())
                    .map(OrganizacaoEntity::getNome)
                    .orElse(null);
        }

        return new Local(
                entity.getId(),
                entity.getNome(),
                entity.getTipo(),
                entity.getEndereco(),
                entity.getCapacidade(),
                entity.getCaracteristicas(),
                entity.isAtivo(),
                entity.getOrganizacaoId(),
                organizacaoNome
        );
    }
}
