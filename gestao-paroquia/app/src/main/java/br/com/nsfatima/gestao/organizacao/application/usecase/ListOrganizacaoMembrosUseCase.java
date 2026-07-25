package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListOrganizacaoMembrosUseCase {

    private final MembroOrganizacaoJpaRepository membershipRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final MembroJpaRepository membroRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public ListOrganizacaoMembrosUseCase(
            MembroOrganizacaoJpaRepository membershipRepository,
            UsuarioJpaRepository usuarioRepository,
            MembroJpaRepository membroRepository,
            OrganizacaoJpaRepository organizacaoRepository) {
        this.membershipRepository = membershipRepository;
        this.usuarioRepository = usuarioRepository;
        this.membroRepository = membroRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizacaoMembroResponse> execute(UUID organizacaoId) {
        var rawMembers = new java.util.ArrayList<>(membershipRepository.findByOrganizacaoIdAndAtivoTrue(organizacaoId));
        var churchMembers = membershipRepository.findByIgrejaVinculoIdAndAtivoTrue(organizacaoId);
        for (var cm : churchMembers) {
            if (rawMembers.stream().noneMatch(m -> m.getUsuarioId().equals(cm.getUsuarioId()))) {
                rawMembers.add(cm);
            }
        }

        List<OrganizacaoMembroResponse> dbMembers = rawMembers.stream()
                .map(m -> {
                    String userNome = usuarioRepository.findById(m.getUsuarioId())
                            .map(u -> u.getNome())
                            .orElse("Usuário Desconhecido");
                    return new OrganizacaoMembroResponse(
                            m.getId(),
                            m.getUsuarioId(),
                            userNome,
                            m.getPapel(),
                            m.isAtivo()
                    );
                })
                .toList();

        List<OrganizacaoMembroResponse> members = new java.util.ArrayList<>(dbMembers);

        var orgOpt = organizacaoRepository.findById(organizacaoId);
        if (orgOpt.isPresent()) {
            var org = orgOpt.get();
            addDynamicMemberIfMissing(members, org.getCoordenador(), "COORDENADOR");
            addDynamicMemberIfMissing(members, org.getViceCoordenador(), "VICE_COORDENADOR");
        }

        return members;
    }

    private void addDynamicMemberIfMissing(List<OrganizacaoMembroResponse> list, String name, String papel) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        boolean exists = list.stream().anyMatch(m -> name.equalsIgnoreCase(m.usuarioNome()));
        if (exists) {
            return;
        }

        UUID userId = usuarioRepository.findAll().stream()
                .filter(u -> name.equalsIgnoreCase(u.getNome()))
                .map(br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity::getId)
                .findFirst()
                .orElse(null);

        if (userId != null) {
            list.add(new OrganizacaoMembroResponse(
                    UUID.randomUUID(), // dummy membership ID
                    userId,
                    name,
                    papel,
                    true
            ));
        }
    }

}
