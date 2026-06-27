package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.api.v1.dto.RegistrarUsuarioRequest;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.MembroOrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegistrarUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final MembroOrganizacaoJpaRepository membershipRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrarUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            MembroOrganizacaoJpaRepository membershipRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.membershipRepository = membershipRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UUID execute(RegistrarUsuarioRequest request) {
        if (usuarioRepository.findByUsernameIgnoreCase(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username/E-mail ja cadastrado: " + request.username());
        }

        UsuarioEntity user = new UsuarioEntity();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setNome(request.nome());
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setTelefone(request.telefone());
        user.setDataNascimento(request.dataNascimento());
        user.setEnabled(false); // Pending approval
        user.setRoles("ROLE_USER"); // Standard non-admin role

        usuarioRepository.save(user);

        if (request.organizacoesSolicitadas() != null) {
            for (UUID orgId : request.organizacoesSolicitadas()) {
                var orgOpt = organizacaoRepository.findById(orgId);
                if (orgOpt.isPresent()) {
                    var org = orgOpt.get();
                    MembroOrganizacaoEntity membership = new MembroOrganizacaoEntity();
                    membership.setId(UUID.randomUUID());
                    membership.setUsuarioId(userId);
                    membership.setOrganizacaoId(org.getId());
                    membership.setTipoOrganizacao(org.getTipo().name());
                    membership.setPapel("MEMBRO");
                    membership.setAtivo(false); // Inactive/Pending approval
                    membershipRepository.save(membership);
                }
            }
        }

        return userId;
    }
}
