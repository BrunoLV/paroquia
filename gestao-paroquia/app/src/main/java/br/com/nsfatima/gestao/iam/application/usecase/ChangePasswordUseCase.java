package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Troca a senha de um usuário já existente por iniciativa do próprio usuário (fluxo de "alterar senha",
 * distinto da redefinição administrativa feita por {@code UsuarioAdminService}). Não valida a senha atual
 * aqui — a checagem de "quem pode chamar isso" é responsabilidade da camada de segurança/controller.
 */
@Service
public class ChangePasswordUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ChangePasswordUseCase(UsuarioJpaRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Substitui a senha do usuário pela nova senha informada, já criptografada antes da persistência —
     * a senha em texto puro nunca é armazenada nem logada.
     * <p>
     * Exemplo: useCase.execute(userId, "novaSenha123")
     *
     * @param id             identificador do usuário
     * @param newRawPassword nova senha em texto puro, a ser criptografada
     * @throws UsuarioNotFoundException se não existir usuário com o id informado
     */
    @Transactional
    public void execute(UUID id, String newRawPassword) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        entity.redefinirSenhaHash(passwordEncoder.encode(newRawPassword));
        usuarioRepository.save(entity);
    }
}
