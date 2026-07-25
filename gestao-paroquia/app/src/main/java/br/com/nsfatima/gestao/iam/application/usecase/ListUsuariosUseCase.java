package br.com.nsfatima.gestao.iam.application.usecase;

import java.util.List;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ListUsuariosUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public ListUsuariosUseCase(
            UsuarioJpaRepository usuarioRepository,
            OrganizacaoJpaRepository organizacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> execute() {
        return usuarioRepository.findAll().stream()
                .map(u -> {
                    UUID igrejaId = u.getIgrejaId();
                    String igrejaNome = null;
                    if (igrejaId != null) {
                        igrejaNome = organizacaoRepository.findById(igrejaId)
                                .map(OrganizacaoEntity::getNome)
                                .orElse(null);
                    }

                    return new UsuarioResponse(
                            u.getId(),
                            u.getUsername(),
                            u.isEnabled(),
                            u.getRoles() == null ? List.of()
                                    : Arrays.stream(u.getRoles().split(",")).map(String::trim).collect(Collectors.toList()),
                            List.of(),
                            u.getNome(),
                            u.getTelefone(),
                            u.getDataNascimento(),
                            igrejaId,
                            igrejaNome,
                            u.isBatizado(),
                            u.isPrimeiraComunhao(),
                            u.isCrismado(),
                            u.isCasadoIgrejaCatolica());

                })
                .toList();
    }
}
