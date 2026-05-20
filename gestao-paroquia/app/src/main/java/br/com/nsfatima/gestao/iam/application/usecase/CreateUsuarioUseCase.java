package br.com.nsfatima.gestao.iam.application.usecase;

import java.util.UUID;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUsuarioUseCase(UsuarioJpaRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    /**
     * Cria uma nova conta de usuário com senha criptografada para permitir o acesso seguro ao sistema.
     * 
     * Exemplo: useCase.execute("novo.usuario", "senhaForte")
     */
    public UUID execute(String username, String rawPassword) {
        if (usuarioRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Username ja existe: " + username);
        }

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        entity.setEnabled(true);

        return usuarioRepository.save(entity).getId();
    }
}
