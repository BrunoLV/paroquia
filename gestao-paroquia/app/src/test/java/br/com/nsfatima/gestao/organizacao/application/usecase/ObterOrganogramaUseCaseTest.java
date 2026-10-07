package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.ClerigoEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.EncargoClericalEntity;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.ClerigoJpaRepository;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.repository.EncargoClericalJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganizacaoMembroResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
class ObterOrganogramaUseCaseTest {

    private OrganizacaoJpaRepository organizacaoRepository;
    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;
    private ListarOrganizacaoMembrosUseCase listMembrosUseCase;
    private EncargoClericalJpaRepository encargoClericalRepository;
    private ClerigoJpaRepository clerigoRepository;
    private ObterOrganogramaUseCase getOrganogramaUseCase;

    @BeforeEach
    void setUp() {
        organizacaoRepository = Mockito.mock(OrganizacaoJpaRepository.class);
        unidadeTerritorialRepository = Mockito.mock(UnidadeTerritorialJpaRepository.class);
        listMembrosUseCase = Mockito.mock(ListarOrganizacaoMembrosUseCase.class);
        encargoClericalRepository = Mockito.mock(EncargoClericalJpaRepository.class);
        clerigoRepository = Mockito.mock(ClerigoJpaRepository.class);
        when(encargoClericalRepository.findByFuncaoAndAtualTrue(Mockito.any())).thenReturn(List.of());
        getOrganogramaUseCase = new ObterOrganogramaUseCase(
                organizacaoRepository, unidadeTerritorialRepository, listMembrosUseCase,
                encargoClericalRepository, clerigoRepository);
    }

    @Test
    void deveConstruirCompletoOrganogramaArvore() {
        UUID matrizId = UUID.randomUUID();
        UnidadeTerritorialEntity matriz = new UnidadeTerritorialEntity();
        matriz.setId(matrizId);
        matriz.setNome("Igreja Matriz");
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setParentId(null);
        matriz.setAtivo(true);

        when(unidadeTerritorialRepository.findAll()).thenReturn(List.of(matriz));

        UUID conselhoId = UUID.randomUUID();
        OrganizacaoEntity conselho = new OrganizacaoEntity();
        conselho.setId(conselhoId);
        conselho.setNome("Conselho");
        conselho.setTipo(TipoOrganizacao.CONSELHO);
        conselho.setCoordenador("Maria do Conselho");
        conselho.setParentId(matrizId);
        conselho.setAtivo(true);

        UUID pastoralId = UUID.randomUUID();
        OrganizacaoEntity pastoral = new OrganizacaoEntity();
        pastoral.setId(pastoralId);
        pastoral.setNome("Pastoral da Catequese");
        pastoral.setTipo(TipoOrganizacao.PASTORAL);
        pastoral.setCoordenador("Maria Oliveira");
        pastoral.setParentId(conselhoId);
        pastoral.setAtivo(true);

        // Mock database call
        when(organizacaoRepository.findAll()).thenReturn(List.of(conselho, pastoral));

        // Mock vinculos
        UUID membroId = UUID.randomUUID();
        OrganizacaoMembroResponse memberResp = new OrganizacaoMembroResponse(
                UUID.randomUUID(),
                membroId,
                "João Santos",
                "membro",
                true,
                true,
                List.of()
        );
        when(listMembrosUseCase.execute(pastoralId)).thenReturn(List.of(memberResp));
        when(listMembrosUseCase.execute(conselhoId)).thenReturn(List.of());

        // Execute Use Case
        OrganogramaNodeResponse root = getOrganogramaUseCase.execute();

        // Verify root node (Paróquia / Governo Eclesiástico)
        assertThat(root.label()).isEqualTo("Igreja Matriz");
        assertThat(root.type()).isEqualTo("PAROQUIA");
        assertThat(root.subtype()).isEqualTo("GOVERNO_ECLESIASTICO");
        assertThat(root.children()).hasSize(1); // Only Igreja Matriz

        // Verify Igreja Matriz (child of Root)
        OrganogramaNodeResponse matrizNode = root.children().getFirst();
        assertThat(matrizNode.label()).isEqualTo("Igreja Matriz");
        assertThat(matrizNode.type()).isEqualTo("UNIDADE_TERRITORIAL");
        assertThat(matrizNode.subtype()).isEqualTo("MATRIZ");
        assertThat(matrizNode.children()).hasSize(1); // Conselho

        // Verify Conselho Node (child of Igreja Matriz)
        OrganogramaNodeResponse conselhoNode = matrizNode.children().getFirst();
        assertThat(conselhoNode.label()).isEqualTo("Conselho");
        assertThat(conselhoNode.type()).isEqualTo("ORGANIZACAO");
        assertThat(conselhoNode.subtype()).isEqualTo("CONSELHO");
        assertThat(conselhoNode.responsavelNome()).isEqualTo("Maria do Conselho");
        assertThat(conselhoNode.children()).hasSize(1); // Contains Pastoral da Catequese!

        // Verify Pastoral Node (child of Conselho)
        OrganogramaNodeResponse pastoralNode = conselhoNode.children().getFirst();
        assertThat(pastoralNode.label()).isEqualTo("Pastoral da Catequese");
        assertThat(pastoralNode.type()).isEqualTo("ORGANIZACAO");
        assertThat(pastoralNode.subtype()).isEqualTo("PASTORAL");
        assertThat(pastoralNode.children()).hasSize(1); // Contains Member

        // Verify Member Node (child of Pastoral)
        OrganogramaNodeResponse memberNode = pastoralNode.children().getFirst();
        assertThat(memberNode.label()).isEqualTo("João Santos");
        assertThat(memberNode.type()).isEqualTo("MEMBRO");
        assertThat(memberNode.subtype()).isEqualTo("membro");
    }

    @Test
    void deveResolverParocoEVigarioDoCleroViaEncargoClerical() {
        UUID matrizId = UUID.randomUUID();
        UnidadeTerritorialEntity matriz = new UnidadeTerritorialEntity();
        matriz.setId(matrizId);
        matriz.setNome("Igreja Matriz");
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setParentId(null);
        matriz.setAtivo(true);

        when(unidadeTerritorialRepository.findAll()).thenReturn(List.of(matriz));
        when(organizacaoRepository.findAll()).thenReturn(List.of());

        UUID parocoId = UUID.randomUUID();
        ClerigoEntity paroco = new ClerigoEntity();
        paroco.setId(parocoId);
        paroco.setNomeEclesiastico("Padre Pedro da Silva");
        paroco.setAtivo(true);
        EncargoClericalEntity encargoParoco = new EncargoClericalEntity();
        encargoParoco.setClerigoId(parocoId);
        when(encargoClericalRepository.findByFuncaoAndAtualTrue(FuncaoEclesiastica.PAROCO))
                .thenReturn(List.of(encargoParoco));
        when(clerigoRepository.findById(parocoId)).thenReturn(Optional.of(paroco));

        UUID vigarioId = UUID.randomUUID();
        ClerigoEntity vigario = new ClerigoEntity();
        vigario.setId(vigarioId);
        vigario.setNomeEclesiastico("Padre João Batista");
        vigario.setAtivo(true);
        EncargoClericalEntity encargoVigario = new EncargoClericalEntity();
        encargoVigario.setClerigoId(vigarioId);
        when(encargoClericalRepository.findByFuncaoAndAtualTrue(FuncaoEclesiastica.VIGARIO_PAROQUIAL))
                .thenReturn(List.of(encargoVigario));
        when(clerigoRepository.findById(vigarioId)).thenReturn(Optional.of(vigario));

        OrganogramaNodeResponse root = getOrganogramaUseCase.execute();

        assertThat(root.subtype()).isEqualTo("GOVERNO_ECLESIASTICO");
        assertThat(root.responsavelNome()).isEqualTo("Padre Pedro da Silva");
        assertThat(root.responsavelPapel()).isEqualTo("PÁROCO");
        assertThat(root.viceResponsavelNome()).isEqualTo("Padre João Batista");
    }
}
