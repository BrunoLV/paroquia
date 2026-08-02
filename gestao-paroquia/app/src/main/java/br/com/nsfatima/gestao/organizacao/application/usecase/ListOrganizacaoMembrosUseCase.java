package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
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

    private final MembroOrganizacaoJpaRepository vinculoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public ListOrganizacaoMembrosUseCase(
            MembroOrganizacaoJpaRepository vinculoRepository,
            UsuarioJpaRepository usuarioRepository,
            MembroJpaRepository membroRepository,
            OrganizacaoJpaRepository organizacaoRepository) {
        this.vinculoRepository = vinculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizacaoMembroResponse> execute(UUID organizacaoId) {
        var rawMembers = new java.util.ArrayList<>(vinculoRepository.findByOrganizacaoIdAndAtivoTrue(organizacaoId));
        var churchMembers = vinculoRepository.findByIgrejaVinculoIdAndAtivoTrue(organizacaoId);
        for (var cm : churchMembers) {
            if (rawMembers.stream().noneMatch(m -> m.getUsuarioId().equals(cm.getUsuarioId()))) {
                rawMembers.add(cm);
            }
        }

        List<OrganizacaoMembroResponse> dbMembers = rawMembers.stream()
                .map(m -> {
                    String userNome = usuarioRepository.findById(m.getUsuarioId())
                            .map(UsuarioEntity::getNome)
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

        usuarioRepository.findAll().stream()
                .filter(u -> name.equalsIgnoreCase(u.getNome()))
                .map(UsuarioEntity::getId)
                .findFirst().ifPresent(userId -> list.add(new OrganizacaoMembroResponse(
                        UUID.randomUUID(), // dummy vinculo ID
                        userId,
                        name,
                        papel,
                        true
                )));

    }

}
