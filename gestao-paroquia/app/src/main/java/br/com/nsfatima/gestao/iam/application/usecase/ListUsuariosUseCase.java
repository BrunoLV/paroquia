package br.com.nsfatima.gestao.iam.application.usecase;

import java.util.List;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ListUsuariosUseCase {

    private final UsuarioJpaRepository usuarioRepository;

    public ListUsuariosUseCase(UsuarioJpaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    /**
     * Recupera a listagem completa de usuários cadastrados para visualização e gestão em painéis administrativos.
     * 
     * Exemplo: useCase.execute()
     */
    public List<UsuarioResponse> execute() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getUsername(), u.isEnabled()))
                .toList();
    }
}
