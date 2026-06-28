package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
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
        clero.setAtivo(true);

        UUID conselhoId = UUID.randomUUID();
        OrganizacaoEntity conselho = new OrganizacaoEntity();
        conselho.setId(conselhoId);
        conselho.setNome("Conselho");
        conselho.setTipo(TipoOrganizacao.CONSELHO);
        conselho.setCoordenador("Maria do Conselho");
        conselho.setAtivo(true);

        UUID pastoralId = UUID.randomUUID();
        OrganizacaoEntity pastoral = new OrganizacaoEntity();
        pastoral.setId(pastoralId);
        pastoral.setNome("Pastoral da Catequese");
        pastoral.setTipo(TipoOrganizacao.PASTORAL);
        pastoral.setCoordenador("Maria Oliveira");
        pastoral.setAtivo(true);

        // Mock database call
        when(organizacaoRepository.findAll()).thenReturn(List.of(clero, conselho, pastoral));

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
        when(listMembrosUseCase.execute(conselhoId)).thenReturn(List.of());

        // Execute Use Case
        OrganogramaNodeResponse root = getOrganogramaUseCase.execute();

        // Verify root node
        assertThat(root.id()).isEqualTo("root");
        assertThat(root.label()).isEqualTo("Paróquia Teste");
        assertThat(root.type()).isEqualTo("PAROQUIA");
        assertThat(root.subtype()).isEqualTo("ROOT");
        assertThat(root.children()).hasSize(3); // Clero, Conselho, Pastorais e Movimentos (Grupo)

        // Verify Clero Node
        OrganogramaNodeResponse cleroNode = root.children().get(0);
        assertThat(cleroNode.label()).isEqualTo("Clero Paroquial");
        assertThat(cleroNode.type()).isEqualTo("ORGANIZACAO");
        assertThat(cleroNode.subtype()).isEqualTo("CLERO");
        assertThat(cleroNode.responsavelNome()).isEqualTo("Padre Pedro");

        // Verify Conselho Node
        OrganogramaNodeResponse conselhoNode = root.children().get(1);
        assertThat(conselhoNode.label()).isEqualTo("Conselho");
        assertThat(conselhoNode.type()).isEqualTo("ORGANIZACAO");
        assertThat(conselhoNode.subtype()).isEqualTo("CONSELHO");
        assertThat(conselhoNode.responsavelNome()).isEqualTo("Maria do Conselho");

        // Verify Pastorais Grupo Node
        OrganogramaNodeResponse groupNode = root.children().get(2);
        assertThat(groupNode.id()).isEqualTo("grupo-pastorais");
        assertThat(groupNode.label()).isEqualTo("Pastorais e Movimentos");
        assertThat(groupNode.type()).isEqualTo("GRUPO");
        assertThat(groupNode.children()).hasSize(1);

        // Verify Pastoral Node
        OrganogramaNodeResponse pastoralNode = groupNode.children().get(0);
        assertThat(pastoralNode.label()).isEqualTo("Pastoral da Catequese");
        assertThat(pastoralNode.type()).isEqualTo("ORGANIZACAO");
        assertThat(pastoralNode.subtype()).isEqualTo("PASTORAL");
        assertThat(pastoralNode.children()).hasSize(1);

        // Verify Member Node
        OrganogramaNodeResponse memberNode = pastoralNode.children().get(0);
        assertThat(memberNode.label()).isEqualTo("João Santos");
        assertThat(memberNode.type()).isEqualTo("MEMBRO");
        assertThat(memberNode.subtype()).isEqualTo("membro");
    }
}
