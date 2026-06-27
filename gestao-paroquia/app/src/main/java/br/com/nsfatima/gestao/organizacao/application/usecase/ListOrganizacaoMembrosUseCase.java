package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListOrganizacaoMembrosUseCase {

    private final MembroOrganizacaoJpaRepository membershipRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final MembroJpaRepository membroRepository;

    public ListOrganizacaoMembrosUseCase(
            MembroOrganizacaoJpaRepository membershipRepository,
            UsuarioJpaRepository usuarioRepository,
            MembroJpaRepository membroRepository) {
        this.membershipRepository = membershipRepository;
        this.usuarioRepository = usuarioRepository;
        this.membroRepository = membroRepository;
    }

    @Transactional(readOnly = true)
    public List<OrganizacaoMembroResponse> execute(UUID organizacaoId) {
        return membershipRepository.findByOrganizacaoIdAndAtivoTrue(organizacaoId).stream()
                .map(m -> {
                    String userNome = usuarioRepository.findById(m.getUsuarioId())
                            .map(u -> u.getNome())
                            .orElse(null);
                    if (userNome == null || userNome.trim().isEmpty()) {
                        userNome = membroRepository.findByUsuarioId(m.getUsuarioId())
                                .map(me -> me.getNomeCompleto())
                                .orElse("Usuário Desconhecido");
                    }
                    return new OrganizacaoMembroResponse(
                            m.getId(),
                            m.getUsuarioId(),
                            userNome,
                            m.getPapel(),
                            m.isAtivo()
                    );
                })
                .toList();
    }
}
