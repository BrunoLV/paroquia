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
import java.util.UUID;

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

        // Identifica e separa as organizações
        OrganizacaoEntity cleroOrg = null;
        OrganizacaoEntity conselhoOrg = null;
        List<OrganizacaoEntity> pastoraisAndMovimentos = new ArrayList<>();

        for (OrganizacaoEntity org : allOrgs) {
            if (!org.isAtivo()) {
                continue;
            }
            // Ignora a organização raiz da paróquia que representa o todo para evitar duplicação no organograma
            if (org.getId().toString().equals("00000000-0000-0000-0000-000000000001")) {
                continue;
            }

            if (org.getTipo() == TipoOrganizacao.CLERO) {
                cleroOrg = org;
            } else if (org.getTipo() == TipoOrganizacao.CONSELHO) {
                conselhoOrg = org;
            } else {
                pastoraisAndMovimentos.add(org);
            }
        }

        List<OrganogramaNodeResponse> rootChildren = new ArrayList<>();

        // 1. Clero Paroquial
        if (cleroOrg != null) {
            rootChildren.add(buildOrgNode(cleroOrg));
        }

        // 2. Conselho Paroquial
        if (conselhoOrg != null) {
            rootChildren.add(buildOrgNode(conselhoOrg));
        }

        // 3. Pastorais e Movimentos (Grupo Virtual)
        if (!pastoraisAndMovimentos.isEmpty()) {
            List<OrganogramaNodeResponse> pastoralNodes = pastoraisAndMovimentos.stream()
                    .map(this::buildOrgNode)
                    .toList();

            rootChildren.add(new OrganogramaNodeResponse(
                    "grupo-pastorais",
                    "Pastorais e Movimentos",
                    "GRUPO",
                    "PASTORAL_GRUPO",
                    null,
                    null,
                    null,
                    pastoralNodes
            ));
        }

        // Nó Raiz do Organograma (A Paróquia)
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

    private OrganogramaNodeResponse buildOrgNode(OrganizacaoEntity org) {
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

        return new OrganogramaNodeResponse(
                org.getId().toString(),
                org.getNome(),
                "ORGANIZACAO",
                org.getTipo().name(),
                responsavelNome,
                responsavelPapel,
                viceResponsavelNome,
                memberNodes
        );
    }
}
