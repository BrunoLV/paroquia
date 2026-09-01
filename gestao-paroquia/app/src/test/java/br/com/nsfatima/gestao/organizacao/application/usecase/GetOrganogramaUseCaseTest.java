package br.com.nsfatima.gestao.organizacao.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.PapelGlobalEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.PapelGlobalJpaRepository;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.GrupoParoquialMembroResponse;
import br.com.nsfatima.gestao.organizacao.api.v1.dto.OrganogramaNodeResponse;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.GrupoParoquialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.GrupoParoquialJpaRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
class GetOrganogramaUseCaseTest {

    private GrupoParoquialJpaRepository grupoParoquialRepository;
    private UnidadeTerritorialJpaRepository unidadeTerritorialRepository;
    private ListGrupoParoquialMembrosUseCase listMembrosUseCase;
    private PapelGlobalJpaRepository papelGlobalRepository;
    private FielJpaRepository fielRepository;
    private GetOrganogramaUseCase getOrganogramaUseCase;

    @BeforeEach
    void setUp() {
        grupoParoquialRepository = Mockito.mock(GrupoParoquialJpaRepository.class);
        unidadeTerritorialRepository = Mockito.mock(UnidadeTerritorialJpaRepository.class);
        listMembrosUseCase = Mockito.mock(ListGrupoParoquialMembrosUseCase.class);
        papelGlobalRepository = Mockito.mock(PapelGlobalJpaRepository.class);
        fielRepository = Mockito.mock(FielJpaRepository.class);
        when(papelGlobalRepository.findByPapelAndAtivoTrue(Mockito.anyString())).thenReturn(List.of());
        getOrganogramaUseCase = new GetOrganogramaUseCase(
                grupoParoquialRepository, unidadeTerritorialRepository, listMembrosUseCase,
                papelGlobalRepository, fielRepository, "Paróquia Teste");
    }

    @Test
    void deveConstruirCompletoOrganogramaArvore() {
        UUID cleroId = UUID.randomUUID();
        UnidadeTerritorialEntity clero = new UnidadeTerritorialEntity();
        clero.setId(cleroId);
        clero.setNome("Clero Paroquial");
        clero.setTipo(TipoUnidadeTerritorial.CLERO);
        clero.setParentId(null);
        clero.setAtivo(true);

        UUID matrizId = UUID.randomUUID();
        UnidadeTerritorialEntity matriz = new UnidadeTerritorialEntity();
        matriz.setId(matrizId);
        matriz.setNome("Igreja Matriz");
        matriz.setTipo(TipoUnidadeTerritorial.MATRIZ);
        matriz.setParentId(cleroId);
        matriz.setAtivo(true);

        when(unidadeTerritorialRepository.findAll()).thenReturn(List.of(clero, matriz));

        UUID conselhoId = UUID.randomUUID();
        GrupoParoquialEntity conselho = new GrupoParoquialEntity();
        conselho.setId(conselhoId);
        conselho.setNome("Conselho");
        conselho.setTipo(TipoGrupo.CONSELHO);
        conselho.setCoordenador("Maria do Conselho");
        conselho.setParentId(matrizId);
        conselho.setAtivo(true);

        UUID pastoralId = UUID.randomUUID();
        GrupoParoquialEntity pastoral = new GrupoParoquialEntity();
        pastoral.setId(pastoralId);
        pastoral.setNome("Pastoral da Catequese");
        pastoral.setTipo(TipoGrupo.PASTORAL);
        pastoral.setCoordenador("Maria Oliveira");
        pastoral.setParentId(conselhoId);
        pastoral.setAtivo(true);

        // Mock database call
        when(grupoParoquialRepository.findAll()).thenReturn(List.of(conselho, pastoral));

        // Mock vinculos
        UUID membroId = UUID.randomUUID();
        GrupoParoquialMembroResponse memberResp = new GrupoParoquialMembroResponse(
                UUID.randomUUID(),
                membroId,
                "João Santos",
                "membro",
                true
        );
        when(listMembrosUseCase.execute(pastoralId)).thenReturn(List.of(memberResp));
        when(listMembrosUseCase.execute(conselhoId)).thenReturn(List.of());

        // Execute Use Case
        OrganogramaNodeResponse root = getOrganogramaUseCase.execute();

        // Verify root node (Clero, agora uma UnidadeTerritorial)
        assertThat(root.label()).isEqualTo("Clero Paroquial");
        assertThat(root.type()).isEqualTo("UNIDADE_TERRITORIAL");
        assertThat(root.subtype()).isEqualTo("CLERO");
        assertThat(root.children()).hasSize(1); // Only Igreja Matriz

        // Verify Igreja Matriz (child of Clero)
        OrganogramaNodeResponse matrizNode = root.children().getFirst();
        assertThat(matrizNode.label()).isEqualTo("Igreja Matriz");
        assertThat(matrizNode.type()).isEqualTo("UNIDADE_TERRITORIAL");
        assertThat(matrizNode.subtype()).isEqualTo("MATRIZ");
        assertThat(matrizNode.children()).hasSize(1); // Conselho

        // Verify Conselho Node (child of Igreja Matriz)
        OrganogramaNodeResponse conselhoNode = matrizNode.children().getFirst();
        assertThat(conselhoNode.label()).isEqualTo("Conselho");
        assertThat(conselhoNode.type()).isEqualTo("GRUPO_PAROQUIAL");
        assertThat(conselhoNode.subtype()).isEqualTo("CONSELHO");
        assertThat(conselhoNode.responsavelNome()).isEqualTo("Maria do Conselho");
        assertThat(conselhoNode.children()).hasSize(1); // Contains Pastoral da Catequese!

        // Verify Pastoral Node (child of Conselho)
        OrganogramaNodeResponse pastoralNode = conselhoNode.children().getFirst();
        assertThat(pastoralNode.label()).isEqualTo("Pastoral da Catequese");
        assertThat(pastoralNode.type()).isEqualTo("GRUPO_PAROQUIAL");
        assertThat(pastoralNode.subtype()).isEqualTo("PASTORAL");
        assertThat(pastoralNode.children()).hasSize(1); // Contains Member

        // Verify Member Node (child of Pastoral)
        OrganogramaNodeResponse memberNode = pastoralNode.children().getFirst();
        assertThat(memberNode.label()).isEqualTo("João Santos");
        assertThat(memberNode.type()).isEqualTo("MEMBRO");
        assertThat(memberNode.subtype()).isEqualTo("membro");
    }

    @Test
    void deveResolverParocoEVigarioDoCleroViaPapelGlobal() {
        UUID cleroId = UUID.randomUUID();
        UnidadeTerritorialEntity clero = new UnidadeTerritorialEntity();
        clero.setId(cleroId);
        clero.setNome("Clero Paroquial");
        clero.setTipo(TipoUnidadeTerritorial.CLERO);
        clero.setParentId(null);
        clero.setAtivo(true);

        when(unidadeTerritorialRepository.findAll()).thenReturn(List.of(clero));
        when(grupoParoquialRepository.findAll()).thenReturn(List.of());

        UUID parocoFielId = UUID.randomUUID();
        FielEntity parocoFiel = new FielEntity();
        parocoFiel.setId(parocoFielId);
        parocoFiel.setNome("Pedro da Silva");
        PapelGlobalEntity parocoPapel = new PapelGlobalEntity();
        parocoPapel.setFielId(parocoFielId);
        parocoPapel.setPapel(PapelGlobal.PAROCO.storedValue());
        when(papelGlobalRepository.findByPapelAndAtivoTrue(PapelGlobal.PAROCO.storedValue()))
                .thenReturn(List.of(parocoPapel));
        when(fielRepository.findById(parocoFielId)).thenReturn(Optional.of(parocoFiel));

        UUID vigarioFielId = UUID.randomUUID();
        FielEntity vigarioFiel = new FielEntity();
        vigarioFiel.setId(vigarioFielId);
        vigarioFiel.setNome("João Batista");
        PapelGlobalEntity vigarioPapel = new PapelGlobalEntity();
        vigarioPapel.setFielId(vigarioFielId);
        vigarioPapel.setPapel(PapelGlobal.VIGARIO.storedValue());
        when(papelGlobalRepository.findByPapelAndAtivoTrue(PapelGlobal.VIGARIO.storedValue()))
                .thenReturn(List.of(vigarioPapel));
        when(fielRepository.findById(vigarioFielId)).thenReturn(Optional.of(vigarioFiel));

        OrganogramaNodeResponse root = getOrganogramaUseCase.execute();

        assertThat(root.subtype()).isEqualTo("CLERO");
        assertThat(root.responsavelNome()).isEqualTo("Pedro da Silva");
        assertThat(root.responsavelPapel()).isEqualTo("PÁROCO");
        assertThat(root.viceResponsavelNome()).isEqualTo("João Batista");
    }
}
