package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.VinculoResponse;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.MembroOrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Recupera os vínculos organizacionais ativos de um usuário, resolvendo os nomes de organização e de
 * igreja/comunidade de contexto para exibição, usado para determinar o escopo de acesso e o nível de
 * atuação do usuário na paróquia.
 */
@Service
public class ListarVinculosUseCase {

    private final MembroOrganizacaoJpaRepository vinculoRepository;
    private final OrganizacaoJpaRepository organizacaoRepository;

    public ListarVinculosUseCase(MembroOrganizacaoJpaRepository vinculoRepository,
                                  OrganizacaoJpaRepository organizacaoRepository) {

        this.vinculoRepository = vinculoRepository;
        this.organizacaoRepository = organizacaoRepository;
    }

    /**
     * Recupera todos os vínculos ativos de um usuário com organizações, com os nomes de organização e de
     * igreja/comunidade de contexto já resolvidos para exibição.
     *
     * @param usuarioId identificador do usuário
     * @return lista de vínculos ativos do usuário
     */
    @Transactional(readOnly = true)
    public List<VinculoResponse> execute(UUID usuarioId) {
        return vinculoRepository.findByUsuarioIdAndAtivoTrue(usuarioId).stream()
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
