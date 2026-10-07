package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador JPA de {@link OrganizacaoRepository}. Além do CRUD básico sobre {@code OrganizacaoEntity}, resolve
 * dinamicamente o nome de exibição de coordenador/apoio a partir dos vínculos de membro ativos
 * (papel {@code coordenador} ou {@code secretario-grupo}/{@code secretario-conselho}), com fallback
 * para o texto livre gravado na própria organização quando nenhum vínculo correspondente é encontrado — e
 * calcula {@code hasDependencies} verificando tanto vínculos de membro quanto eventos associados, para que
 * {@code OrganizacaoService} possa bloquear exclusões que deixariam referências órfãs.
 */
@Repository
public class OrganizacaoRepositoryImpl implements OrganizacaoRepository {

    private static final String PAPEL_COORDENADOR = "coordenador";
    private static final String PAPEL_SECRETARIO = "secretario";

    private final OrganizacaoJpaRepository organizacaoJpaRepository;
    private final MembroGrupoJpaRepository membroGrupoJpaRepository;
    private final EventoJpaRepository eventoJpaRepository;
    private final FielJpaRepository fielRepository;

    public OrganizacaoRepositoryImpl(
            OrganizacaoJpaRepository organizacaoJpaRepository,
            MembroGrupoJpaRepository membroGrupoJpaRepository,
            EventoJpaRepository eventoJpaRepository,
            FielJpaRepository fielRepository) {
        this.organizacaoJpaRepository = organizacaoJpaRepository;
        this.membroGrupoJpaRepository = membroGrupoJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.fielRepository = fielRepository;
    }

    @Override
    public void save(Organizacao domain) {
        OrganizacaoEntity entity = new OrganizacaoEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setTipo(domain.getTipo());
        entity.setContato(domain.getContato());
        entity.setCoordenador(domain.getCoordenador());
        entity.setViceCoordenador(domain.getViceCoordenador());
        entity.setAtivo(domain.isAtivo());
        entity.setParentId(domain.getParentId());
        entity.setPastoralOficialId(domain.getPastoralOficialId());
        entity.setSetorPastoral(domain.getSetorPastoral());
        entity.setSubtipoConselho(domain.getSubtipoConselho());
        organizacaoJpaRepository.save(entity);
    }

    @Override
    public Optional<Organizacao> findById(UUID id) {
        return organizacaoJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<Organizacao> findAll() {
        return organizacaoJpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasDependencies(UUID id) {
        boolean hasMembers = membroGrupoJpaRepository.existsByOrganizacaoId(id);
        if (hasMembers) {
            return true;
        }

        // Note: EventoJpaRepository needs existsByOrganizacaoResponsavelId
        return eventoJpaRepository.existsByOrganizacaoResponsavelId(id);
    }

    @Override
    public void delete(UUID id) {
        organizacaoJpaRepository.deleteById(id);
    }

    private Organizacao toDomain(OrganizacaoEntity entity) {
        String coordName = resolveRoleMemberName(entity.getId(), PAPEL_COORDENADOR, entity.getCoordenador());
        String viceName = resolveRoleMemberName(entity.getId(), PAPEL_SECRETARIO, entity.getViceCoordenador());

        return new Organizacao(
                entity.getId(),
                entity.getNome(),
                entity.getTipo(),
                entity.getContato(),
                coordName,
                viceName,
                entity.isAtivo(),
                entity.getParentId(),
                entity.getPastoralOficialId(),
                entity.getSetorPastoral(),
                entity.getSubtipoConselho()
        );
    }

    private String resolveRoleMemberName(UUID orgId, String roleKeyword, String fallback) {
        return membroGrupoJpaRepository.findByOrganizacaoIdAndAtivoTrue(orgId).stream()
                .filter(m -> {
                    if (m.getPapel() == null) {
                        return false;
                    }
                    String p = m.getPapel().toLowerCase();
                    if (PAPEL_COORDENADOR.equalsIgnoreCase(roleKeyword)) {
                        return "coordenador".equals(p);
                    }
                    if (PAPEL_SECRETARIO.equalsIgnoreCase(roleKeyword)) {
                        return p.startsWith(PAPEL_SECRETARIO);
                    }
                    return p.contains(roleKeyword.toLowerCase());
                })
                .findFirst()
                .map(m -> {
                    String name = fielRepository.findById(m.getFielId())
                            .map(FielEntity::getNome)
                            .orElse(fallback);
                    return (name != null && !name.trim().isEmpty()) ? name : fallback;
                })
                .orElse(fallback);
    }

}
