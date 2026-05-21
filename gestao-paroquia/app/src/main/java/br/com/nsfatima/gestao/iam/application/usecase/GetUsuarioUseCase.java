package br.com.nsfatima.gestao.iam.application.usecase;

import java.util.UUID;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;

    public GetUsuarioUseCase(UsuarioJpaRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    /**
     * Busca os dados de um usuário pelo seu identificador único para fornecer informações de perfil e status.
     * 
     * Exemplo: useCase.execute(userId)
     */
    public UsuarioResponse execute(UUID id) {
        return usuarioRepository.findById(id)
                .map(u -> new UsuarioResponse(
                        u.getId(),
                        u.getUsername(),
                        u.isEnabled(),
                        u.getRoles() == null ? List.of()
                                : Arrays.stream(u.getRoles().split(",")).map(String::trim).collect(Collectors.toList())))
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }
}
