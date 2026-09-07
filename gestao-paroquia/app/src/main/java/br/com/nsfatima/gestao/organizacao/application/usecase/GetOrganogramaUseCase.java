package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.EncargoClericalEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.EncargoClericalJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.GrupoParoquialMembroResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.springframework.beans.factory.annotation.Value;
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
 * {@code UnidadeTerritorial} e {@code GrupoParoquial}, respectivamente. A raiz preferida é a unidade
 * territorial do tipo {@code CLERO}; se ela não existir, todas as raízes (unidades territoriais e grupos
 * sem organização-mãe) são expostas como filhas diretas de um nó sintético "root" com o nome da paróquia.
 * Grupos do tipo {@code CONSELHO} são filhos de uma unidade territorial (Matriz/Comunidade); Pastoral e
 * Movimento são filhos de um Conselho; Ministério não exige organização-mãe. Apenas nós ativos entram na
 * árvore, e cada nó de grupo paroquial recebe também seus membros como nós-folha filhos.
 */
@Service
public class GetOrganogramaUseCase {

    private static final String GRUPO_PAROQUIAL_RAIZ_ID = "00000000-0000-0000-0000-000000000001";

    private final GrupoParoquialJpaRepository grupoParoquialRepository;
    private final UnidadeTerritorialJpaRepository unidadeTerritorialRepository;
    private final ListGrupoParoquialMembrosUseCase listMembrosUseCase;
    private final EncargoClericalJpaRepository encargoClericalRepository;
    private final ClerigoJpaRepository clerigoRepository;
    private final String paroquiaNome;

    public GetOrganogramaUseCase(
            GrupoParoquialJpaRepository grupoParoquialRepository,
            UnidadeTerritorialJpaRepository unidadeTerritorialRepository,
            ListGrupoParoquialMembrosUseCase listMembrosUseCase,
            EncargoClericalJpaRepository encargoClericalRepository,
            ClerigoJpaRepository clerigoRepository,
            @Value("${app.paroquia.nome:Paróquia Nossa Senhora de Fátima}") String paroquiaNome) {
        this.grupoParoquialRepository = grupoParoquialRepository;
        this.unidadeTerritorialRepository = unidadeTerritorialRepository;
        this.listMembrosUseCase = listMembrosUseCase;
        this.encargoClericalRepository = encargoClericalRepository;
        this.clerigoRepository = clerigoRepository;
        this.paroquiaNome = paroquiaNome;
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
        List<GrupoParoquialEntity> todosGrupos = grupoParoquialRepository.findAll();

        List<UnidadeTerritorialEntity> unidadesRaiz = todasUnidades.stream()
                .filter(UnidadeTerritorialEntity::isAtivo)
                .filter(u -> u.getParentId() == null)
                .toList();

        UnidadeTerritorialEntity rootClero = unidadesRaiz.stream()
                .filter(u -> u.getTipo() == TipoUnidadeTerritorial.CLERO)
                .findFirst()
                .orElse(null);

        Map<UUID, List<UnidadeTerritorialEntity>> unidadesFilhasPorPaiId = todasUnidades.stream()
                .filter(UnidadeTerritorialEntity::isAtivo)
                .filter(u -> u.getParentId() != null)
                .collect(Collectors.groupingBy(UnidadeTerritorialEntity::getParentId));

        Map<UUID, List<GrupoParoquialEntity>> gruposPorPaiId = todosGrupos.stream()
                .filter(GrupoParoquialEntity::isAtivo)
                .filter(org -> !org.getId().toString().equals(GRUPO_PAROQUIAL_RAIZ_ID))
                .filter(org -> org.getParentId() != null)
                .collect(Collectors.groupingBy(GrupoParoquialEntity::getParentId));

        List<GrupoParoquialEntity> gruposRaiz = todosGrupos.stream()
                .filter(GrupoParoquialEntity::isAtivo)
                .filter(org -> !org.getId().toString().equals(GRUPO_PAROQUIAL_RAIZ_ID))
                .filter(org -> org.getParentId() == null)
                .toList();

        if (rootClero != null) {
            return buildUnidadeNodeRecursively(rootClero, unidadesFilhasPorPaiId, gruposPorPaiId);
        }

        // Fallback caso não haja unidade territorial do clero raiz
        List<OrganogramaNodeResponse> rootChildren = new ArrayList<>();
        unidadesRaiz.forEach(u -> rootChildren.add(
                buildUnidadeNodeRecursively(u, unidadesFilhasPorPaiId, gruposPorPaiId)));
        gruposRaiz.forEach(org -> rootChildren.add(buildOrgNodeRecursively(org, gruposPorPaiId)));

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

    private OrganogramaNodeResponse buildUnidadeNodeRecursively(
            UnidadeTerritorialEntity unidade,
            Map<UUID, List<UnidadeTerritorialEntity>> unidadesFilhasPorPaiId,
            Map<UUID, List<GrupoParoquialEntity>> gruposPorPaiId) {

        List<UnidadeTerritorialEntity> unidadesFilhas = unidadesFilhasPorPaiId.getOrDefault(unidade.getId(), List.of());
        List<OrganogramaNodeResponse> nosFilhos = unidadesFilhas.stream()
                .map(child -> buildUnidadeNodeRecursively(child, unidadesFilhasPorPaiId, gruposPorPaiId))
                .collect(Collectors.toCollection(ArrayList::new));

        List<GrupoParoquialEntity> gruposFilhos = gruposPorPaiId.getOrDefault(unidade.getId(), List.of());
        gruposFilhos.forEach(org -> nosFilhos.add(buildOrgNodeRecursively(org, gruposPorPaiId)));

        boolean isClero = unidade.getTipo() == TipoUnidadeTerritorial.CLERO;
        String responsavelPapel = isClero ? "PÁROCO" : null;
        String responsavelNome = isClero ? findClerigoNomePorFuncao(FuncaoEclesiastica.PAROCO) : null;
        String viceResponsavelNome = isClero ? findClerigoNomePorFuncao(FuncaoEclesiastica.VIGARIO_PAROQUIAL) : null;

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

    private OrganogramaNodeResponse buildOrgNodeRecursively(GrupoParoquialEntity org,
                                                            Map<UUID, List<GrupoParoquialEntity>> filhosPorPaiId) {

        List<GrupoParoquialEntity> organizacoesFilhas = filhosPorPaiId.getOrDefault(org.getId(), List.of());
        List<OrganogramaNodeResponse> nosFilhos = organizacoesFilhas.stream()
                .map(child -> buildOrgNodeRecursively(child, filhosPorPaiId))
                .collect(Collectors.toCollection(ArrayList::new));

        List<GrupoParoquialMembroResponse> membros = listMembrosUseCase.execute(org.getId());

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

        // Encontrar nomes do responsável (coordenador) e vice-responsável
        String responsavelNome = org.getCoordenador();
        String responsavelPapel = "COORDENADOR";
        String viceResponsavelNome = org.getViceCoordenador();

        // Fallback caso não estejam definidos na organização mas estejam nos membros
        if (responsavelNome == null || responsavelNome.isBlank()) {
            responsavelNome = membros.stream()
                    .filter(m -> "COORDENADOR".equalsIgnoreCase(m.papel()) || "PAROCO".equalsIgnoreCase(m.papel()))
                    .map(GrupoParoquialMembroResponse::fielNome)
                    .findFirst()
                    .orElse(null);
        }
        if (viceResponsavelNome == null || viceResponsavelNome.isBlank()) {
            viceResponsavelNome = membros.stream()
                    .filter(m -> "VICE_COORDENADOR".equalsIgnoreCase(m.papel())
                            || "VIGARIO".equalsIgnoreCase(m.papel()))
                    .map(GrupoParoquialMembroResponse::fielNome)
                    .findFirst()
                    .orElse(null);
        }

        nosFilhos.addAll(nosMembros);

        return new OrganogramaNodeResponse(
                org.getId().toString(),
                org.getNome(),
                "GRUPO_PAROQUIAL",
                org.getTipo().name(),
                responsavelNome,
                responsavelPapel,
                viceResponsavelNome,
                nosFilhos
        );
    }
}
