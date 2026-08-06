package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.GrupoParoquial;
import br.com.nsfatima.gestao.organizacao.domain.repository.GrupoParoquialRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Adaptador JPA de {@link GrupoParoquialRepository}. Além do CRUD básico sobre {@code GrupoParoquialEntity}, resolve
 * dinamicamente o nome de exibição de coordenador/vice-coordenador a partir dos vínculos de membro ativos
 * (papel contendo "coord"/"presidente"/"mesa" para coordenador, PAPEL_VICE para vice-coordenador), com fallback
 * para o texto livre gravado na própria organização quando nenhum vínculo correspondente é encontrado — e
 * calcula {@code hasDependencies} verificando tanto vínculos de membro quanto eventos associados, para que
 * {@code GrupoParoquialService} possa bloquear exclusões que deixariam referências órfãs.
 */
@Repository
public class GrupoParoquialRepositoryImpl implements GrupoParoquialRepository {

    private static final String PAPEL_COORDENADOR = "coordenador";
    private static final String PAPEL_VICE = "vice";

    private final GrupoParoquialJpaRepository grupoParoquialJpaRepository;
    private final MembroGrupoJpaRepository membroGrupoJpaRepository;
    private final EventoJpaRepository eventoJpaRepository;
    private final FielJpaRepository fielRepository;

    public GrupoParoquialRepositoryImpl(
            GrupoParoquialJpaRepository grupoParoquialJpaRepository,
            MembroGrupoJpaRepository membroGrupoJpaRepository,
            EventoJpaRepository eventoJpaRepository,
            FielJpaRepository fielRepository) {
        this.grupoParoquialJpaRepository = grupoParoquialJpaRepository;
        this.membroGrupoJpaRepository = membroGrupoJpaRepository;
        this.eventoJpaRepository = eventoJpaRepository;
        this.fielRepository = fielRepository;
    }

    @Override
    public void save(GrupoParoquial domain) {
        GrupoParoquialEntity entity = new GrupoParoquialEntity();
        entity.setId(domain.getId());
        entity.setNome(domain.getNome());
        entity.setTipo(domain.getTipo());
        entity.setContato(domain.getContato());
        entity.setCoordenador(domain.getCoordenador());
        entity.setViceCoordenador(domain.getViceCoordenador());
        entity.setAtivo(domain.isAtivo());
        entity.setParentId(domain.getParentId());
        entity.setPastoralOficialId(domain.getPastoralOficialId());
        grupoParoquialJpaRepository.save(entity);
    }

    @Override
    public Optional<GrupoParoquial> findById(UUID id) {
        return grupoParoquialJpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public List<GrupoParoquial> findAll() {
        return grupoParoquialJpaRepository.findAll().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean hasDependencies(UUID id) {
        boolean hasMembers = membroGrupoJpaRepository.existsByGrupoParoquialId(id);
        if (hasMembers) {
            return true;
        }

        // Note: EventoJpaRepository needs existsByGrupoParoquialResponsavelId
        return eventoJpaRepository.existsByGrupoParoquialResponsavelId(id);
    }

    @Override
    public void delete(UUID id) {
        grupoParoquialJpaRepository.deleteById(id);
    }

    private GrupoParoquial toDomain(GrupoParoquialEntity entity) {
        String coordName = resolveRoleMemberName(entity.getId(), PAPEL_COORDENADOR, entity.getCoordenador());
        String viceName = resolveRoleMemberName(entity.getId(), PAPEL_VICE, entity.getViceCoordenador());

        return new GrupoParoquial(
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
        return membroGrupoJpaRepository.findByGrupoParoquialIdAndAtivoTrue(orgId).stream()
                .filter(m -> {
                    if (m.getPapel() == null) {
                        return false;
                    }
                    String p = m.getPapel().toLowerCase();
                    if (PAPEL_COORDENADOR.equalsIgnoreCase(roleKeyword)) {
                        return (p.contains("coord") || p.contains("coordenador")
                                || p.contains("presidente") || p.contains("mesa"))
                                && !p.contains(PAPEL_VICE);
                    }
                    if (PAPEL_VICE.equalsIgnoreCase(roleKeyword)) {
                        return p.contains(PAPEL_VICE);
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
