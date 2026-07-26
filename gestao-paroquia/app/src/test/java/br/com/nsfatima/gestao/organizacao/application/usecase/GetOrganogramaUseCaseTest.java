package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GetOrganogramaUseCaseTest {

    private OrganizacaoJpaRepository organizacaoRepository;
    private ListOrganizacaoMembrosUseCase listMembrosUseCase;
    private GetOrganogramaUseCase getOrganogramaUseCase;

    @BeforeEach
    void setUp() {
        organizacaoRepository = Mockito.mock(OrganizacaoJpaRepository.class);
        listMembrosUseCase = Mockito.mock(ListOrganizacaoMembrosUseCase.class);
        getOrganogramaUseCase = new GetOrganogramaUseCase(organizacaoRepository, listMembrosUseCase, "Paróquia Teste");
    }

    @Test
    void shouldBuildCompleteOrganogramaTree() {
        UUID cleroId = UUID.randomUUID();
        OrganizacaoEntity clero = new OrganizacaoEntity();
        clero.setId(cleroId);
        clero.setNome("Clero Paroquial");
        clero.setTipo(TipoOrganizacao.CLERO);
        clero.setCoordenador("Padre Pedro");
        clero.setParentId(null);
        clero.setAtivo(true);

        UUID matrizId = UUID.randomUUID();
        OrganizacaoEntity matriz = new OrganizacaoEntity();
        matriz.setId(matrizId);
        matriz.setNome("Igreja Matriz");
        matriz.setTipo(TipoOrganizacao.MATRIZ);
        matriz.setParentId(cleroId);
        matriz.setAtivo(true);

        UUID conselhoId = UUID.randomUUID();
        OrganizacaoEntity conselho = new OrganizacaoEntity();
        conselho.setId(conselhoId);
        conselho.setNome("Conselho");
        conselho.setTipo(TipoOrganizacao.CONSELHO_PASTORAL);
        conselho.setCoordenador("Maria do Conselho");
        conselho.setParentId(matrizId);
        conselho.setAtivo(true);

        UUID conselhoEconomicoId = UUID.randomUUID();
        OrganizacaoEntity conselhoEconomico = new OrganizacaoEntity();
        conselhoEconomico.setId(conselhoEconomicoId);
        conselhoEconomico.setNome("Conselho Econômico");
        conselhoEconomico.setTipo(TipoOrganizacao.CONSELHO_ECONOMICO);
        conselhoEconomico.setCoordenador("Pedro Finanças");
        conselhoEconomico.setParentId(matrizId);
        conselhoEconomico.setAtivo(true);

        UUID pastoralId = UUID.randomUUID();
        OrganizacaoEntity pastoral = new OrganizacaoEntity();
        pastoral.setId(pastoralId);
        pastoral.setNome("Pastoral da Catequese");
        pastoral.setTipo(TipoOrganizacao.PASTORAL);
        pastoral.setCoordenador("Maria Oliveira");
        pastoral.setParentId(conselhoId);
        pastoral.setAtivo(true);

        // Mock database call
        when(organizacaoRepository.findAll()).thenReturn(List.of(clero, matriz, conselho, conselhoEconomico, pastoral));

        // Mock memberships
        UUID membroId = UUID.randomUUID();
        OrganizacaoMembroResponse memberResp = new OrganizacaoMembroResponse(
                UUID.randomUUID(),
                membroId,
                "João Santos",
                "membro",
                true
        );
        when(listMembrosUseCase.execute(pastoralId)).thenReturn(List.of(memberResp));
        when(listMembrosUseCase.execute(cleroId)).thenReturn(List.of());
        when(listMembrosUseCase.execute(matrizId)).thenReturn(List.of());
        when(listMembrosUseCase.execute(conselhoId)).thenReturn(List.of());
        when(listMembrosUseCase.execute(conselhoEconomicoId)).thenReturn(List.of());

        // Execute Use Case
        OrganogramaNodeResponse root = getOrganogramaUseCase.execute();

        // Verify root node (now is Clero!)
        assertThat(root.label()).isEqualTo("Clero Paroquial");
        assertThat(root.type()).isEqualTo("ORGANIZACAO");
        assertThat(root.subtype()).isEqualTo("CLERO");
        assertThat(root.responsavelNome()).isEqualTo("Padre Pedro");
        assertThat(root.children()).hasSize(1); // Only Igreja Matriz

        // Verify Igreja Matriz (child of Clero)
        OrganogramaNodeResponse matrizNode = root.children().get(0);
        assertThat(matrizNode.label()).isEqualTo("Igreja Matriz");
        assertThat(matrizNode.type()).isEqualTo("ORGANIZACAO");
        assertThat(matrizNode.subtype()).isEqualTo("MATRIZ");
        assertThat(matrizNode.children()).hasSize(2); // Conselho Pastoral, Conselho Econômico

        // Verify Conselho Node (child of Igreja Matriz)
        OrganogramaNodeResponse conselhoNode = matrizNode.children().stream()
                .filter(c -> "CONSELHO_PASTORAL".equals(c.subtype()))
                .findFirst().orElseThrow();
        assertThat(conselhoNode.label()).isEqualTo("Conselho");
        assertThat(conselhoNode.responsavelNome()).isEqualTo("Maria do Conselho");
        assertThat(conselhoNode.children()).hasSize(1); // Contains Pastoral da Catequese!

        // Verify Conselho Econômico Node (child of Igreja Matriz)
        OrganogramaNodeResponse conselhoEconomicoNode = matrizNode.children().stream()
                .filter(c -> "CONSELHO_ECONOMICO".equals(c.subtype()))
                .findFirst().orElseThrow();
        assertThat(conselhoEconomicoNode.label()).isEqualTo("Conselho Econômico");
        assertThat(conselhoEconomicoNode.responsavelNome()).isEqualTo("Pedro Finanças");
        assertThat(conselhoEconomicoNode.children()).isEmpty();

        // Verify Pastoral Node (child of Conselho Pastoral)
        OrganogramaNodeResponse pastoralNode = conselhoNode.children().get(0);
        assertThat(pastoralNode.label()).isEqualTo("Pastoral da Catequese");
        assertThat(pastoralNode.type()).isEqualTo("ORGANIZACAO");
        assertThat(pastoralNode.subtype()).isEqualTo("PASTORAL");
        assertThat(pastoralNode.children()).hasSize(1); // Contains Member

        // Verify Member Node (child of Pastoral)
        OrganogramaNodeResponse memberNode = pastoralNode.children().get(0);
        assertThat(memberNode.label()).isEqualTo("João Santos");
        assertThat(memberNode.type()).isEqualTo("MEMBRO");
        assertThat(memberNode.subtype()).isEqualTo("membro");
    }
}
