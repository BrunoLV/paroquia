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
        OrganizacaoEntity conselhoEconomicoOrg = null;
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
            } else if (org.getTipo() == TipoOrganizacao.CONSELHO_ECONOMICO) {
                conselhoEconomicoOrg = org;
            } else {
                pastoraisAndMovimentos.add(org);
            }
        }

        // 1. Pastorais e Movimentos (filhos do Conselho Pastoral)
        List<OrganogramaNodeResponse> pastoralNodes = pastoraisAndMovimentos.stream()
                .map(org -> buildOrgNode(org, null))
                .toList();

        // 2. Conselho Pastoral Paroquial (com as pastorais como filhos)
        OrganogramaNodeResponse conselhoPastoralNode = null;
        if (conselhoOrg != null) {
            conselhoPastoralNode = buildOrgNode(conselhoOrg, pastoralNodes);
        }

        // 3. Conselho Econômico Paroquial (sem filhos adicionais, apenas membros normais se houver)
        OrganogramaNodeResponse conselhoEconomicoNode = null;
        if (conselhoEconomicoOrg != null) {
            conselhoEconomicoNode = buildOrgNode(conselhoEconomicoOrg, null);
        }

        // 4. Clero Paroquial (Nó raiz)
        List<OrganogramaNodeResponse> cleroChildren = new ArrayList<>();
        if (conselhoPastoralNode != null) {
            cleroChildren.add(conselhoPastoralNode);
        }
        if (conselhoEconomicoNode != null) {
            cleroChildren.add(conselhoEconomicoNode);
        }

        if (cleroOrg != null) {
            return buildOrgNode(cleroOrg, cleroChildren);
        }

        // Fallback caso não haja organização do clero (não deve ocorrer na prática)
        return new OrganogramaNodeResponse(
                "root",
                paroquiaNome,
                "PAROQUIA",
                "ROOT",
                null,
                null,
                null,
                cleroChildren
        );
    }

    private OrganogramaNodeResponse buildOrgNode(OrganizacaoEntity org, List<OrganogramaNodeResponse> children) {
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

        // Se passamos uma lista customizada de filhos, usamos ela. Caso contrário, os memberNodes.
        List<OrganogramaNodeResponse> finalChildren = children != null ? children : memberNodes;

        return new OrganogramaNodeResponse(
                org.getId().toString(),
                org.getNome(),
                "ORGANIZACAO",
                org.getTipo().name(),
                responsavelNome,
                responsavelPapel,
                viceResponsavelNome,
                finalChildren
        );
    }
}
