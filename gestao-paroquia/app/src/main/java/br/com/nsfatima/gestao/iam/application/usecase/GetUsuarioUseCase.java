package br.com.nsfatima.gestao.iam.application.usecase;

import java.util.UUID;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.domain.exception.UsuarioNotFoundException;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GetUsuarioUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final MembroJpaRepository membroRepository;
    private final br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository organizacaoRepository;

    public GetUsuarioUseCase(
            UsuarioJpaRepository usuarioRepository,
            MembroJpaRepository membroRepository,
            br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository organizacaoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.membroRepository = membroRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    @Transactional(readOnly = true)
    public UsuarioResponse execute(UUID id) {
        return usuarioRepository.findById(id)
                .map(u -> {
                    String nome = u.getNome();
                    String telefone = u.getTelefone();
                    java.time.LocalDate dataNascimento = u.getDataNascimento();


                    java.util.UUID igrejaId = u.getIgrejaId();
                    String igrejaNome = null;
                    if (igrejaId != null) {
                        igrejaNome = organizacaoRepository.findById(igrejaId)
                                .map(br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity::getNome)
                                .orElse(null);
                    }

                    return new UsuarioResponse(
                            u.getId(),
                            u.getUsername(),
                            u.isEnabled(),
                            u.getRoles() == null ? List.of()
                                    : Arrays.stream(u.getRoles().split(",")).map(String::trim).collect(Collectors.toList()),
                            List.of(),
                            nome,
                            telefone,
                            dataNascimento,
                            igrejaId,
                            igrejaNome,
                            u.isBatizado(),
                            u.isPrimeiraComunhao(),
                            u.isCrismado(),
                            u.isCasadoIgrejaCatolica());

                })
                .orElseThrow(() -> new UsuarioNotFoundException(id));
    }
}
