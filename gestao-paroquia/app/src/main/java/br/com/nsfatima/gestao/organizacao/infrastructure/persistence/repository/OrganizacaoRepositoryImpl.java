package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;
import br.com.nsfatima.gestao.organizacao.domain.repository.OrganizacaoRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;

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
            UsuarioJpaRepository usuarioRepository) {
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
        if (hasMembers) return true;

        // Note: EventoJpaRepository needs existsByOrganizacaoResponsavelId
        return eventoJpaRepository.existsByOrganizacaoResponsavelId(id);
    }

    @Override
    public void delete(UUID id) {
        organizacaoJpaRepository.deleteById(id);
    }

    private Organizacao toDomain(OrganizacaoEntity entity) {
        String coordName = resolveCoordinatorName(entity.getId(), PapelOrganizacional.COORDENADOR.name(), entity.getCoordenador());
        String viceName = resolveCoordinatorName(entity.getId(), PapelOrganizacional.VICE_COORDENADOR.name(), entity.getViceCoordenador());

        return new Organizacao(
                entity.getId(),
                entity.getNome(),
                entity.getTipo(),
                entity.getContato(),
                coordName,
                viceName,
                entity.isAtivo()
        );
    }

    private String resolveCoordinatorName(UUID orgId, String papel, String fallback) {
        return membroOrganizacaoJpaRepository.findByOrganizacaoIdAndPapelAndAtivoTrue(orgId, papel)
                .flatMap(m -> usuarioRepository.findById(m.getUsuarioId()))
                .map(u -> u.getNome())
                .orElse(fallback);
    }
}
