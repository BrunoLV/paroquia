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
                        m.usuarioId().toString(),
                        m.usuarioNome(),
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
                    .map(OrganizacaoMembroResponse::usuarioNome)
                    .findFirst()
                    .orElse(null);
        }
        if (viceResponsavelNome == null || viceResponsavelNome.isBlank()) {
            viceResponsavelNome = membros.stream()
                    .filter(m -> "VICE_COORDENADOR".equalsIgnoreCase(m.papel())
                            || "VIGARIO".equalsIgnoreCase(m.papel()))
                    .map(OrganizacaoMembroResponse::usuarioNome)
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
