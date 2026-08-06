package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Monta a árvore hierárquica (organograma) de toda a estrutura organizacional da paróquia a partir das
 * organizações e seus vínculos de membro. A raiz preferida é a organização do tipo {@code CLERO}; se ela não
 * existir, todas as organizações-raiz (sem organização-mãe) são expostas como filhas diretas de um nó
 * sintético "root" com o nome da paróquia. Apenas organizações ativas entram na árvore, e cada nó de
 * organização recebe também seus membros como nós-folha filhos, junto com o nome do responsável/coordenador
 * (e vice), resolvido preferencialmente do campo de texto livre da organização e, na ausência dele, a partir
 * dos vínculos de membro com papel de coordenador/vice.
 */
@Service
public class GetOrganogramaUseCase {

    private static final String ORGANIZACAO_RAIZ_ID = "00000000-0000-0000-0000-000000000001";

    private final OrganizacaoJpaRepository organizacaoRepository;
    private final ListOrganizacaoMembrosUseCase listMembrosUseCase;
    private final String paroquiaNome;

    public GetOrganogramaUseCase(
            OrganizacaoJpaRepository organizacaoRepository,
            ListOrganizacaoMembrosUseCase listMembrosUseCase,
            @Value("${app.paroquia.nome:Paróquia Nossa Senhora de Fátima}") String paroquiaNome) {
        this.organizacaoRepository = organizacaoRepository;
        this.listMembrosUseCase = listMembrosUseCase;
        this.paroquiaNome = paroquiaNome;
    }

    /**
     * Monta e retorna a árvore completa do organograma paroquial, partindo da organização raiz do clero
     * (ou de um nó sintético "root" caso ela não exista) e descendo recursivamente por todas as organizações
     * ativas e seus membros.
     *
     * @return o nó raiz da árvore do organograma, com todos os descendentes já montados
     */
    @Transactional(readOnly = true)
    public OrganogramaNodeResponse execute() {
        List<OrganizacaoEntity> todasOrganizacoes = organizacaoRepository.findAll();

        List<OrganizacaoEntity> organizacoesRaiz = todasOrganizacoes.stream()
                .filter(OrganizacaoEntity::isAtivo)
                .filter(org -> !org.getId().toString().equals(ORGANIZACAO_RAIZ_ID))
                .filter(org -> org.getParentId() == null)
                .toList();

        OrganizacaoEntity rootClero = organizacoesRaiz.stream()
                .filter(org -> org.getTipo() == TipoOrganizacao.CLERO)
                .findFirst()
                .orElse(null);

        Map<UUID, List<OrganizacaoEntity>> filhosPorPaiId = todasOrganizacoes.stream()
                .filter(OrganizacaoEntity::isAtivo)
                .filter(org -> !org.getId().toString().equals(ORGANIZACAO_RAIZ_ID))
                .filter(org -> org.getParentId() != null)
                .collect(Collectors.groupingBy(OrganizacaoEntity::getParentId));

        if (rootClero != null) {
            return buildOrgNodeRecursively(rootClero, filhosPorPaiId);
        }

        // Fallback caso não haja organização do clero raiz
        List<OrganogramaNodeResponse> rootChildren = organizacoesRaiz.stream()
                .map(org -> buildOrgNodeRecursively(org, filhosPorPaiId))
                .toList();

        return new OrganogramaNodeResponse(
                "root",
                paroquiaNome,
                "PAROQUIA",
                "ROOT",
                null,
                null,
                null,
                rootChildren
        );
    }

    private OrganogramaNodeResponse buildOrgNodeRecursively(OrganizacaoEntity org,
                                                            Map<UUID, List<OrganizacaoEntity>> filhosPorPaiId) {

        List<OrganizacaoEntity> organizacoesFilhas = filhosPorPaiId.getOrDefault(org.getId(), List.of());
        List<OrganogramaNodeResponse> nosFilhos = organizacoesFilhas.stream()
                .map(child -> buildOrgNodeRecursively(child, filhosPorPaiId))
                .collect(Collectors.toCollection(ArrayList::new));

        List<OrganizacaoMembroResponse> membros = listMembrosUseCase.execute(org.getId());

        List<OrganogramaNodeResponse> nosMembros = membros.stream()
                .map(m -> new OrganogramaNodeResponse(
                        m.fielId().toString(),
                        m.fielNome(),
                        "MEMBRO",
                        m.papel(),
                        null,
                        null,
                        null,
                        List.of()
                ))
                .toList();

        // Encontrar nomes do responsável (coordenador/pároco) e vice-responsável
        String responsavelNome = org.getCoordenador();
        String responsavelPapel = org.getTipo() == TipoOrganizacao.CLERO ? "PÁROCO" : "COORDENADOR";
        String viceResponsavelNome = org.getViceCoordenador();

        // Fallback caso não estejam definidos na organização mas estejam nos membros
        if (responsavelNome == null || responsavelNome.isBlank()) {
            responsavelNome = membros.stream()
                    .filter(m -> "COORDENADOR".equalsIgnoreCase(m.papel()) || "PAROCO".equalsIgnoreCase(m.papel()))
                    .map(OrganizacaoMembroResponse::fielNome)
                    .findFirst()
                    .orElse(null);
        }
        if (viceResponsavelNome == null || viceResponsavelNome.isBlank()) {
            viceResponsavelNome = membros.stream()
                    .filter(m -> "VICE_COORDENADOR".equalsIgnoreCase(m.papel())
                            || "VIGARIO".equalsIgnoreCase(m.papel()))
                    .map(OrganizacaoMembroResponse::fielNome)
                    .findFirst()
                    .orElse(null);
        }

        nosFilhos.addAll(nosMembros);

        return new OrganogramaNodeResponse(
                org.getId().toString(),
                org.getNome(),
                "ORGANIZACAO",
                org.getTipo().name(),
                responsavelNome,
                responsavelPapel,
                viceResponsavelNome,
                nosFilhos
        );
    }
}
