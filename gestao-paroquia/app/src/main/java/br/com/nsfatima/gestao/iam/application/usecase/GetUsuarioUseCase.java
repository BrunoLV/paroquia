package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Recupera os detalhes completos de um usuário para exibição/edição administrativa.
 */
@Service
public class GetUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final GrupoParoquialJpaRepository grupoParoquialRepository;
    private final FielJpaRepository fielRepository;
    private final ClerigoJpaRepository clerigoRepository;

    public GetUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            GrupoParoquialJpaRepository grupoParoquialRepository,
            FielJpaRepository fielRepository,
            ClerigoJpaRepository clerigoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.grupoParoquialRepository = grupoParoquialRepository;
        this.fielRepository = fielRepository;
        this.clerigoRepository = clerigoRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse execute(UUID id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    String nome = null;
                    String telefone = null;
                    LocalDate dataNascimento = null;

                    if (u.getFielId() != null) {
                        FielEntity fiel = fielRepository.findById(u.getFielId()).orElse(null);
                        if (fiel != null) {
                            nome = fiel.getNome();
                            telefone = fiel.getCelular();
                            dataNascimento = fiel.getDataNascimento();
                        }
                    } else if (u.getClerigoId() != null) {
                        ClerigoEntity clerigo = clerigoRepository.findById(u.getClerigoId()).orElse(null);
                        if (clerigo != null) {
                            nome = clerigo.getNomeEclesiastico();
                            telefone = clerigo.getCelular();
                            dataNascimento = clerigo.getDataNascimento();
                        }
                    }

                    UUID igrejaId = u.getIgrejaId();
                    String igrejaNome = null;
                    if (igrejaId != null) {
                        igrejaNome = grupoParoquialRepository.findById(igrejaId)
                                .map(GrupoParoquialEntity::getNome)
                                .orElse(null);
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
                            telefone,
                            dataNascimento,
                            igrejaId,
                            igrejaNome,
                            false, false, false, false);

                })
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }
}
