package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Cria uma conta de usuário pela via administrativa ({@code ROLE_ADMIN}) — único caminho de criação de
 * {@code Usuario} no sistema, já que o autocadastro público foi removido. O usuário criado por este caminho
 * já nasce habilitado ({@code enabled=true}) e sem papel/vínculo organizacional atribuído — quem cria decide
 * depois os papéis e vínculos, se necessário.
 */
@Service
public class CreateUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUsuarioUseCase(UsuarioJpaRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Cria o usuário com a senha já criptografada, rejeitando duplicidade de username (case-insensitive).
     * <p>
     * Exemplo: useCase.execute("novo.usuario", "senhaForte")
     *
     * @param username    nome de usuário/login desejado; deve ser único ignorando maiúsculas/minúsculas
     * @param rawPassword senha em texto puro, a ser criptografada antes da persistência
     * @return o id gerado para o novo usuário
     * @throws IllegalArgumentException se já existir usuário com o mesmo username (ignorando caixa)
     */
    @Transactional
    public UUID execute(String username, String rawPassword) {
        if (usuarioRepository.findByUsernameIgnoreCase(username).isPresent()) {
            throw new IllegalArgumentException("Username ja existe: " + username);
        }

        UsuarioEntity entity = new UsuarioEntity();
        entity.setId(UUID.randomUUID());
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode(rawPassword));
        entity.setEnabled(true);

        String actor = resolveActor();
        entity.setCriadoPor(actor);
        entity.setAtualizadoPor(actor);

        return usuarioRepository.save(entity).getId();
    }

    private String resolveActor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }
}
