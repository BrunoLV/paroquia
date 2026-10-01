package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNaoEncontradoException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Recupera os detalhes de um usuário para exibição/edição administrativa no IAM.
 */
@Service
public class ObterUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final FielJpaRepository fielRepository;
    private final ClerigoJpaRepository clerigoRepository;

    public ObterUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            FielJpaRepository fielRepository,
            ClerigoJpaRepository clerigoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.fielRepository = fielRepository;
        this.clerigoRepository = clerigoRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse execute(UUID id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    String nome = null;

                    if (u.getFielId() != null) {
                        FielEntity fiel = fielRepository.findById(u.getFielId()).orElse(null);
                        if (fiel != null) {
                            nome = fiel.getNome();
                        }
                    } else if (u.getClerigoId() != null) {
                        ClerigoEntity clerigo = clerigoRepository.findById(u.getClerigoId()).orElse(null);
                        if (clerigo != null) {
                            nome = clerigo.getNomeEclesiastico();
                        }
                    }

                    return new UsuarioResponse(
                            u.getId(),
                            u.getUsername(),
                            u.isEnabled(),
                            u.getRoles() == null ? List.of()
                                    : Arrays.stream(u.getRoles().split(",")).map(String::trim)
                                    .collect(Collectors.toList()),
                            List.of(),
                            nome,
                            u.getFielId(),
                            u.getClerigoId());
                })
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));
    }
}
