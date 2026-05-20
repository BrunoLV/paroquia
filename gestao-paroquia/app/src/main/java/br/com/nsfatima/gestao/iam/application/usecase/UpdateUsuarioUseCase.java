package br.com.nsfatima.gestao.iam.application.usecase;

import java.util.UUID;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;

    public UpdateUsuarioUseCase(UsuarioJpaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    /**
     * Atualiza os campos editáveis de um usuário para refletir mudanças cadastrais ou de permissão de acesso.
     * 
     * Exemplo: useCase.execute(userId, "novo.username", true)
     */
    public void execute(UUID id, String username, Boolean enabled) {
        UsuarioEntity entity = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNotFoundException(id));

        if (username != null) {
            entity.setUsername(username);
        }
        if (enabled != null) {
            entity.setEnabled(enabled);
        }
        usuarioRepository.save(entity);
    }
}
