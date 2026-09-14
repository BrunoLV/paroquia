package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Cria uma conta de usuário pela via administrativa ({@code ROLE_ADMIN}). Exige vínculo exclusivo
 * com {@code Fiel} ou {@code Clerigo}. O usuário já nasce habilitado ({@code enabled=true}).
 */
@Service
public class CriarUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final FielJpaRepository fielRepository;
    private final ClerigoJpaRepository clerigoRepository;
    private final PasswordEncoder passwordEncoder;

    public CriarUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            FielJpaRepository fielRepository,
            ClerigoJpaRepository clerigoRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.fielRepository = fielRepository;
        this.clerigoRepository = clerigoRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UUID execute(String username, String rawPassword, UUID fielId) {
        return execute(username, rawPassword, fielId, null);
    }

    @Transactional
    public UUID execute(String username, String rawPassword, UUID fielId, UUID clerigoId) {
        if ((fielId == null && clerigoId == null) || (fielId != null && clerigoId != null)) {
            throw new IllegalArgumentException("Usuario deve estar vinculado exclusivamente a um Fiel ou a um Clerigo");
        }

        if (usuarioRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Username ja existe: " + username);
        }

        if (fielId != null && fielRepository.findById(fielId).isEmpty()) {
            throw new IllegalArgumentException("Fiel nao encontrado: " + fielId);
        }

        if (clerigoId != null && clerigoRepository.findById(clerigoId).isEmpty()) {
            throw new IllegalArgumentException("Clerigo nao encontrado: " + clerigoId);
        }

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        entity.setFielId(fielId);
        entity.setClerigoId(clerigoId);
        entity.setEnabled(true);

        String actor = resolverAtor();
        entity.setCriadoPor(actor);
        entity.setAtualizadoPor(actor);

        return usuarioRepository.save(entity).getId();
    }

    private String resolverAtor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}
