package br.com.nsfatima.gestao.iam.application.usecase;

import java.util.UUID;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChangePasswordUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordUseCase(UsuarioJpaRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    /**
     * Altera a senha de um usuário no banco de dados para garantir que apenas o portador da nova credencial tenha acesso ao sistema.
     * 
     * Exemplo: useCase.execute(userId, "novaSenha123")
     */
    public void execute(UUID id, String newRawPassword) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        entity.setPasswordHash(passwordEncoder.encode(newRawPassword));
        usuarioRepository.save(entity);
    }
}
