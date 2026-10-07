package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.VinculoResponse;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Recupera os vínculos organizacionais ativos do Fiel de um usuário, resolvendo os nomes de organização e de
 * igreja/comunidade de contexto para exibição, usado para determinar o escopo de acesso e o nível de
 * atuação do usuário na paróquia.
 */
@Service
public class ListarVinculosUseCase {

    private final MembroGrupoJpaRepository vinculoRepository;
    private final UsuarioJpaRepository usuarioRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public ListarVinculosUseCase(MembroGrupoJpaRepository vinculoRepository,
                                  UsuarioJpaRepository usuarioRepository,
                                  OrganizacaoJpaRepository organizacaoRepository) {

        this.vinculoRepository = vinculoRepository;
        this.usuarioRepository = usuarioRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    /**
     * Recupera todos os vínculos ativos do Fiel do usuário informado com organizações, com os nomes de
     * organização e de igreja/comunidade de contexto já resolvidos para exibição.
     *
     * @param usuarioId identificador do usuário
     * @return lista de vínculos ativos do Fiel do usuário
     * @throws IllegalArgumentException se o usuário não existir
     */
    @Transactional(readOnly = true)
    public List<VinculoResponse> execute(UUID usuarioId) {
        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario nao encontrado: " + usuarioId));

        if (usuario.getFielId() == null) {
            return List.of();
        }

        return vinculoRepository.findByFielIdAndAtivoTrue(usuario.getFielId()).stream()
                .map(m -> {
                    String orgNome = organizacaoRepository.findById(m.getOrganizacaoId())
                            .map(OrganizacaoEntity::getNome)
                            .orElse(null);
                    String igrejaNome = m.getIgrejaVinculoId() != null
                            ? organizacaoRepository.findById(m.getIgrejaVinculoId())
                            .map(OrganizacaoEntity::getNome).orElse(null)
                            : null;
                    return new VinculoResponse(
                            m.getId(),
                            m.getOrganizacaoId(),
                            orgNome,
                            m.getTipoOrganizacao().name(),
                            m.getPapel(),
                            m.isAtivo(),
                            m.getIgrejaVinculoId(),
                            igrejaNome);
                })
                .toList();
    }
}
