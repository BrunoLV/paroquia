package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.api.v1.dto.UsuarioResponse;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lista todos os usuários cadastrados no sistema para fins de auditoria e gestão administrativa global
 * (endpoint restrito a admin/clero pároco/coordenador de conselho). Resolve o nome de exibição da
 * igreja/comunidade e os dados cadastrais pastorais (via {@code Fiel} vinculado) de cada usuário, mas não
 * carrega seus vínculos organizacionais individuais — para não pagar o custo de N+1 consultas ao listar
 * todos os usuários.
 */
@Service
public class ListUsuariosUseCase {

    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;
    private final FielJpaRepository fielRepository;

    public ListUsuariosUseCase(
            UsuarioJpaRepository usuarioRepository,
            OrganizacaoJpaRepository organizacaoRepository,
            FielJpaRepository fielRepository) {
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
        this.fielRepository = fielRepository;
    }

    /**
     * Retorna todos os usuários com seus dados cadastrais e papéis globais convertidos a partir da string
     * persistida, sem os vínculos organizacionais (sempre {@code List.of()} na resposta).
     *
     * @return lista de todos os usuários cadastrados
     */
    @Transactional(readOnly = true)
    public List<UsuarioResponse> execute() {
        var usuarios = usuarioRepository.findAll();
        var fieisPorId = fielRepository.findAllById(
                        usuarios.stream()
                                .map(UsuarioEntity::getFielId)
                                .filter(Objects::nonNull)
                                .toList())
                .stream()
                .collect(Collectors.toMap(FielEntity::getId, f -> f));

        return usuarios.stream()
                .map(u -> {
                    FielEntity fiel = fieisPorId.get(u.getFielId());

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
                                    : Arrays.stream(u.getRoles().split(","))
                                    .map(String::trim).collect(Collectors.toList()),
                            List.of(),
                            fiel == null ? null : fiel.getNome(),
                            fiel == null ? null : fiel.getTelefone(),
                            fiel == null ? null : fiel.getDataNascimento(),
                            igrejaId,
                            igrejaNome,
                            fiel != null && fiel.isBatizado(),
                            fiel != null && fiel.isPrimeiraComunhao(),
                            fiel != null && fiel.isCrismado(),
                            fiel != null && fiel.isCasadoIgrejaCatolica());

                })
                .toList();
    }
}
