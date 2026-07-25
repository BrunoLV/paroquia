package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.api.v1.dto.RegistrarUsuarioRequest;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class RegistrarUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrarUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UUID execute(RegistrarUsuarioRequest request) {
        if (usuarioRepository.findByUsernameIgnoreCase(request.username()).isPresent()) {
            throw new IllegalArgumentException("Username/E-mail ja cadastrado: " + request.username());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdminOrParoco = auth != null && auth.isAuthenticated() && 
            auth.getAuthorities().stream().anyMatch(a -> 
                "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_CLERO_PAROCO".equals(a.getAuthority()));

        if (request.igrejaId() == null) {
            throw new IllegalArgumentException("A igreja/comunidade e obrigatoria para o cadastro do usuario");
        }
        var igrejaTargetOpt = organizacaoRepository.findById(request.igrejaId());
        if (igrejaTargetOpt.isEmpty()) {
            throw new IllegalArgumentException("Igreja nao encontrada: " + request.igrejaId());
        }
        var igrejaTarget = igrejaTargetOpt.get();
        if (igrejaTarget.getTipo() != br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao.MATRIZ 
                && igrejaTarget.getTipo() != br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao.COMUNIDADE) {
            throw new IllegalArgumentException("A igreja selecionada deve ser do tipo MATRIZ ou COMUNIDADE");
        }

        UsuarioEntity user = new UsuarioEntity();
        UUID userId = UUID.randomUUID();
        user.setId(userId);
        user.setNome(request.nome());
        user.setUsername(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setTelefone(request.telefone());
        user.setDataNascimento(request.dataNascimento());
        user.setIgrejaId(request.igrejaId());
        user.setBatizado(Boolean.TRUE.equals(request.batizado()));
        user.setPrimeiraComunhao(Boolean.TRUE.equals(request.primeiraComunhao()));
        user.setCrismado(Boolean.TRUE.equals(request.crismado()));
        user.setCasadoIgrejaCatolica(Boolean.TRUE.equals(request.casadoIgrejaCatolica()));
        user.setEnabled(isAdminOrParoco); // Active directly if registered by admin

        user.setRoles("ROLE_USER"); // Standard non-admin role

        usuarioRepository.save(user);

        return userId;
    }
}
