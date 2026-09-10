package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Atualização parcial (patch) de username e estado de habilitação de um usuário. Restrito a
 * {@code ROLE_ADMIN} no controller — não passa pela checagem fina de coordenador de organização usada em
 * outras operações administrativas de usuário.
 */
@Service
public class UpdateUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;

    public UpdateUsuarioUseCase(UsuarioJpaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Aplica apenas os campos informados (não nulos): {@code username} e/ou {@code enabled}. Campos nulos
     * são preservados com o valor atual, permitindo atualização parcial sem sobrescrever o restante do
     * cadastro.
     * <p>
     * Exemplo: useCase.execute(userId, "novo.username", true)
     *
     * @param id       identificador do usuário
     * @param username novo username, ou {@code null} para não alterar
     * @param enabled  novo estado de habilitação, ou {@code null} para não alterar
     * @throws UsuarioNotFoundException se não existir usuário com o id informado
     */
    @Transactional
    public void execute(UUID id, String username, Boolean enabled) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        if (username != null) {
            entity.alterarUsername(username);
        }
        if (enabled != null) {
            if (enabled) {
                entity.ativar();
            } else {
                entity.desativar();
            }
        }
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        entity.setAtualizadoPor(authentication != null ? authentication.getName() : null);

        usuarioRepository.save(entity);
    }
}
