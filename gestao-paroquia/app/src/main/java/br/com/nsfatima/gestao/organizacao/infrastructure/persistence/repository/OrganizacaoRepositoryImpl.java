package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class OrganizacaoRepositoryImpl implements OrganizacaoRepository {

    private final OrganizacaoJpaRepository organizacaoJpaRepository;
    private final MembroOrganizacaoJpaRepository membroOrganizacaoJpaRepository;
    private final EventoJpaRepository eventoJpaRepository;
    private final UsuarioJpaRepository usuarioRepository;

    public OrganizacaoRepositoryImpl(
            OrganizacaoJpaRepository organizacaoJpaRepository,
            MembroOrganizacaoJpaRepository membroOrganizacaoJpaRepository,
            EventoJpaRepository eventoJpaRepository,
            UsuarioJpaRepository usuarioRepository,
            MembroJpaRepository membroRepository) {
        this.organizacaoJpaRepository = organizacaoJpaRepository;
        this.membroOrganizacaoJpaRepository = membroOrganizacaoJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.usuarioRepository = usuarioRepository;
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
        boolean hasMembers = membroOrganizacaoJpaRepository.existsByOrganizacaoId(id);
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
        String coordName = resolveRoleMemberName(entity.getId(), "coordenador", entity.getCoordenador());
        String viceName = resolveRoleMemberName(entity.getId(), "vice", entity.getViceCoordenador());

        return new Organizacao(
                entity.getId(),
                entity.getNome(),
                entity.getTipo(),
                entity.getContato(),
                coordName,
                viceName,
                entity.isAtivo(),
                entity.getParentId(),
                entity.getPastoralOficialId()
        );
    }

    private String resolveRoleMemberName(UUID orgId, String roleKeyword, String fallback) {
        return membroOrganizacaoJpaRepository.findByOrganizacaoIdAndAtivoTrue(orgId).stream()
                .filter(m -> {
                    if (m.getPapel() == null) return false;
                    String p = m.getPapel().toLowerCase();
                    if ("coordenador".equalsIgnoreCase(roleKeyword)) {
                        return (p.contains("coord") || p.contains("coordenador") || p.contains("presidente") || p.contains("mesa"))
                                && !p.contains("vice");
                    }
                    if ("vice".equalsIgnoreCase(roleKeyword)) {
                        return p.contains("vice");
                    }
                    return p.contains(roleKeyword.toLowerCase());
                })
                .findFirst()
                .map(m -> {
                    String name = usuarioRepository.findById(m.getUsuarioId())
                            .map(UsuarioEntity::getNome)
                            .orElse(fallback);
                    return (name != null && !name.trim().isEmpty()) ? name : fallback;
                })
                .orElse(fallback);
    }

}
