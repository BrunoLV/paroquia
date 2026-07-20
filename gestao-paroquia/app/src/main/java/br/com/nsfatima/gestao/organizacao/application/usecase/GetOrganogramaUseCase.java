package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
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
        List<OrganizacaoEntity> allOrgs = organizacaoRepository.findAll();

        List<OrganizacaoEntity> rootOrgs = allOrgs.stream()
                .filter(OrganizacaoEntity::isAtivo)
                .filter(org -> !org.getId().toString().equals("00000000-0000-0000-0000-000000000001"))
                .filter(org -> org.getParentId() == null)
                .toList();

        OrganizacaoEntity rootClero = rootOrgs.stream()
                .filter(org -> org.getTipo() == TipoOrganizacao.CLERO)
                .findFirst()
                .orElse(null);

        Map<UUID, List<OrganizacaoEntity>> childrenByParentId = allOrgs.stream()
                .filter(OrganizacaoEntity::isAtivo)
                .filter(org -> !org.getId().toString().equals("00000000-0000-0000-0000-000000000001"))
                .filter(org -> org.getParentId() != null)
                .collect(Collectors.groupingBy(OrganizacaoEntity::getParentId));

        if (rootClero != null) {
            return buildOrgNodeRecursively(rootClero, childrenByParentId);
        }

        // Fallback caso não haja organização do clero raiz
        List<OrganogramaNodeResponse> rootChildren = rootOrgs.stream()
                .map(org -> buildOrgNodeRecursively(org, childrenByParentId))
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

    private OrganogramaNodeResponse buildOrgNodeRecursively(OrganizacaoEntity org, Map<UUID, List<OrganizacaoEntity>> childrenByParentId) {
        List<OrganizacaoEntity> childOrgs = childrenByParentId.getOrDefault(org.getId(), List.of());
        List<OrganogramaNodeResponse> childNodes = childOrgs.stream()
                .map(child -> buildOrgNodeRecursively(child, childrenByParentId))
                .collect(Collectors.toCollection(ArrayList::new));

        List<OrganizacaoMembroResponse> membros = listMembrosUseCase.execute(org.getId());

        List<OrganogramaNodeResponse> memberNodes = membros.stream()
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
                    .filter(m -> "VICE_COORDENADOR".equalsIgnoreCase(m.papel()) || "VIGARIO".equalsIgnoreCase(m.papel()))
                    .map(OrganizacaoMembroResponse::usuarioNome)
                    .findFirst()
                    .orElse(null);
        }

        childNodes.addAll(memberNodes);

        return new OrganogramaNodeResponse(
                org.getId().toString(),
                org.getNome(),
                "ORGANIZACAO",
                org.getTipo().name(),
                responsavelNome,
                responsavelPapel,
                viceResponsavelNome,
                childNodes
        );
    }
}
