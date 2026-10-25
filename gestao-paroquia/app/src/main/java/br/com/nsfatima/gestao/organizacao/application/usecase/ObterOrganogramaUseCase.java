package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.EncargoClericalEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.EncargoClericalJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Monta a árvore hierárquica (organograma) de toda a estrutura da paróquia. Desde a track
 * {@code linguagem_ubiqua_dominio}, a hierarquia territorial (Clero/Matriz/Comunidade) e os grupos
 * paroquiais (Conselho/Pastoral/Movimento/Ministério) vivem em agregados distintos —
 * {@code UnidadeTerritorial} e {@code Organizacao}, respectivamente. A raiz preferida é a unidade
 * territorial do tipo {@code CLERO}; se ela não existir, todas as raízes (unidades territoriais e grupos
 * sem organização-mãe) são expostas como filhas diretas de um nó sintético "root" com o nome da paróquia.
 * Grupos do tipo {@code CONSELHO} são filhos de uma unidade territorial (Matriz/Comunidade); Pastoral e
 * Movimento são filhos de um Conselho; Ministério não exige organização-mãe. Apenas nós ativos entram na
 * árvore, e cada nó de grupo paroquial recebe também seus membros como nós-folha filhos.
 */
@Service
public class ObterOrganogramaUseCase {

    private static final String ORGANIZACAO_RAIZ_ID = "00000000-0000-0000-0000-000000000001";

    private final OrganizacaoJpaRepository organizacaoRepository;
    private final UnidadeTerritorialJpaRepository unidadeTerritorialRepository;
    private final ListarOrganizacaoMembrosUseCase listMembrosUseCase;
    private final EncargoClericalJpaRepository encargoClericalRepository;
    private final ClerigoJpaRepository clerigoRepository;
    public ObterOrganogramaUseCase(
            OrganizacaoJpaRepository organizacaoRepository,
            UnidadeTerritorialJpaRepository unidadeTerritorialRepository,
            ListarOrganizacaoMembrosUseCase listMembrosUseCase,
            EncargoClericalJpaRepository encargoClericalRepository,
            ClerigoJpaRepository clerigoRepository) {
        this.organizacaoRepository = organizacaoRepository;
        this.unidadeTerritorialRepository = unidadeTerritorialRepository;
        this.listMembrosUseCase = listMembrosUseCase;
        this.encargoClericalRepository = encargoClericalRepository;
        this.clerigoRepository = clerigoRepository;
    }

    /**
     * Monta e retorna a árvore completa do organograma paroquial, partindo da unidade territorial raiz do
     * clero (ou de um nó sintético "root" caso ela não exista) e descendo recursivamente por todas as
     * unidades territoriais, grupos paroquiais ativos e seus membros.
     *
     * @return o nó raiz da árvore do organograma, com todos os descendentes já montados
     */
    @Transactional(readOnly = true)
    public OrganogramaNodeResponse execute() {
        List<UnidadeTerritorialEntity> todasUnidades = unidadeTerritorialRepository.findAll();
        List<OrganizacaoEntity> todosGrupos = organizacaoRepository.findAll();

        List<UnidadeTerritorialEntity> unidadesRaiz = todasUnidades.stream()
                .filter(UnidadeTerritorialEntity::isAtivo)
                .filter(u -> u.getParentId() == null)
                .toList();

        Map<UUID, List<UnidadeTerritorialEntity>> unidadesFilhasPorPaiId = todasUnidades.stream()
                .filter(UnidadeTerritorialEntity::isAtivo)
                .filter(u -> u.getParentId() != null)
                .collect(Collectors.groupingBy(UnidadeTerritorialEntity::getParentId));

        Map<UUID, List<OrganizacaoEntity>> gruposPorPaiId = todosGrupos.stream()
                .filter(OrganizacaoEntity::isAtivo)
                .filter(org -> !org.getId().toString().equals(ORGANIZACAO_RAIZ_ID))
                .filter(org -> org.getParentId() != null)
                .collect(Collectors.groupingBy(OrganizacaoEntity::getParentId));

        List<OrganizacaoEntity> gruposRaiz = todosGrupos.stream()
                .filter(OrganizacaoEntity::isAtivo)
                .filter(org -> !org.getId().toString().equals(ORGANIZACAO_RAIZ_ID))
                .filter(org -> org.getParentId() == null)
                .toList();

        String parocoNome = findClerigoNomePorFuncao(FuncaoEclesiastica.PAROCO);
        String vigarioNome = findClerigoNomePorFuncao(FuncaoEclesiastica.VIGARIO_PAROQUIAL);

        List<OrganogramaNodeResponse> rootChildren = new ArrayList<>();
        unidadesRaiz.forEach(u -> rootChildren.add(
                buildUnidadeNodeRecursively(u, unidadesFilhasPorPaiId, gruposPorPaiId)));
        gruposRaiz.forEach(org -> rootChildren.add(buildOrgNodeRecursively(org, gruposPorPaiId)));

        String paroquiaNome = todasUnidades.stream()
                .filter(u -> u.getTipo() == TipoUnidadeTerritorial.MATRIZ && u.isAtivo())
                .map(UnidadeTerritorialEntity::getNome)
                .findFirst()
                .orElse("Paróquia Nossa Senhora de Fátima");

        return new OrganogramaNodeResponse(
                "root",
                paroquiaNome,
                "PAROQUIA",
                "GOVERNO_ECLESIASTICO",
                parocoNome,
                "PÁROCO",
                vigarioNome,
                rootChildren
        );
    }

    private OrganogramaNodeResponse buildUnidadeNodeRecursively(
            UnidadeTerritorialEntity unidade,
            Map<UUID, List<UnidadeTerritorialEntity>> unidadesFilhasPorPaiId,
            Map<UUID, List<OrganizacaoEntity>> gruposPorPaiId) {

        List<UnidadeTerritorialEntity> unidadesFilhas = unidadesFilhasPorPaiId.getOrDefault(unidade.getId(), List.of());
        List<OrganogramaNodeResponse> nosFilhos = unidadesFilhas.stream()
                .map(child -> buildUnidadeNodeRecursively(child, unidadesFilhasPorPaiId, gruposPorPaiId))
                .collect(Collectors.toCollection(ArrayList::new));

        List<OrganizacaoEntity> gruposFilhos = gruposPorPaiId.getOrDefault(unidade.getId(), List.of());
        gruposFilhos.forEach(org -> nosFilhos.add(buildOrgNodeRecursively(org, gruposPorPaiId)));

        boolean isMatriz = unidade.getTipo() == TipoUnidadeTerritorial.MATRIZ;
        String responsavelPapel = isMatriz ? "PÁROCO" : "RESPONSÁVEL PASTORAL";
        String responsavelNome = null;
        if (unidade.getClerigoResponsavelId() != null) {
            responsavelNome = clerigoRepository.findById(unidade.getClerigoResponsavelId())
                    .filter(c -> c.isAtivo())
                    .map(c -> c.getNomeEclesiastico())
                    .orElse(null);
        } else if (isMatriz) {
            responsavelNome = findClerigoNomePorFuncao(FuncaoEclesiastica.PAROCO);
        }
        String viceResponsavelNome = isMatriz ? findClerigoNomePorFuncao(FuncaoEclesiastica.VIGARIO_PAROQUIAL) : null;

        return new OrganogramaNodeResponse(
                unidade.getId().toString(),
                unidade.getNome(),
                "UNIDADE_TERRITORIAL",
                unidade.getTipo().name(),
                responsavelNome,
                responsavelPapel,
                viceResponsavelNome,
                nosFilhos
        );
    }

    /** Resolve liderança a partir do encargo clerical atual, nunca de cadastro leigo. */
    private String findClerigoNomePorFuncao(FuncaoEclesiastica funcao) {
        return encargoClericalRepository.findByFuncaoAndAtualTrue(funcao).stream()
                .findFirst().map(EncargoClericalEntity::getClerigoId)
                .flatMap(clerigoRepository::findById)
                .filter(clerigo -> clerigo.isAtivo())
                .map(clerigo -> clerigo.getNomeEclesiastico())
                .orElse(null);
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

        // A coordenação é derivada dos vínculos: coordenador e vice-coordenador (secretário de grupo) são membros.
        String responsavelNome = nomeComPapel(membros, "COORDENADOR");
        String responsavelPapel = "COORDENADOR";
        String viceResponsavelNome = nomeComPapel(membros, "SECRETARIO_GRUPO");

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

    private static String nomeComPapel(List<OrganizacaoMembroResponse> membros, String papel) {
        return membros.stream()
                .filter(m -> papel.equalsIgnoreCase(m.papel().replace('-', '_')))
                .map(OrganizacaoMembroResponse::fielNome)
                .findFirst()
                .orElse(null);
    }
}
