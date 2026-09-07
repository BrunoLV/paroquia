package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Recupera os detalhes completos de um usuário para exibição/edição administrativa, incluindo o nome da
 * igreja/comunidade vinculada resolvido a partir do {@code igrejaId} bruto persistido na entidade, e os
 * dados cadastrais pastorais resolvidos a partir do {@code Fiel} vinculado (Contexto Paroquial — o
 * {@code Usuario} não carrega mais esses dados diretamente).
 */
@Service
public class GetUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final GrupoParoquialJpaRepository grupoParoquialRepository;
    private final FielJpaRepository fielRepository;

    public GetUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            GrupoParoquialJpaRepository grupoParoquialRepository,
            FielJpaRepository fielRepository) {
        this.usuarioRepository = usuarioRepository;
        this.grupoParoquialRepository = grupoParoquialRepository;
        this.fielRepository = fielRepository;
    }

    /**
     * Monta a resposta completa do usuário, convertendo a string de papéis persistida (separada por vírgulas)
     * em uma coleção, resolvendo o nome de exibição da igreja/comunidade vinculada, quando houver, e os
     * dados cadastrais pastorais a partir do {@code Fiel} vinculado. Não inclui vínculos organizacionais
     * (membros) — essa lista é obtida separadamente.
     *
     * @param id identificador do usuário
     * @return dados completos do usuário
     * @throws UsuarioNotFoundException se não existir usuário com o id informado
     */
    @Transactional(readOnly = true)
    public UsuarioResponse execute(UUID id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    FielEntity fiel = fielRepository.findById(u.getFielId()).orElse(null);

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
                            fiel == null ? null : fiel.getNome(),
                            fiel == null ? null : fiel.getCelular(),
                            fiel == null ? null : fiel.getDataNascimento(),
                            igrejaId,
                            igrejaNome,
                            false, false, false, false);

                })
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }
}
