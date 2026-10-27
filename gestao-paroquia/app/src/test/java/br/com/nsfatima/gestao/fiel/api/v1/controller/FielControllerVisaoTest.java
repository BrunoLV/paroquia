package br.com.nsfatima.gestao.fiel.api.v1.controller;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.fiel.api.v1.dto.FielNomeResponse;
import br.com.nsfatima.gestao.fiel.application.usecase.AtualizarFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.CriarFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarCicloVidaFielUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.ListarFieisUseCase;
import br.com.nsfatima.gestao.fiel.application.usecase.ObterFielUseCase;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FielControllerVisaoTest {

    private ListarFieisUseCase listar;
    private ObterFielUseCase obter;
    private EventoActorContextResolver resolver;
    private MembroGrupoJpaRepository membros;
    private FielController controller;
    private final UUID orgId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        listar = mock(ListarFieisUseCase.class);
        obter = mock(ObterFielUseCase.class);
        resolver = mock(EventoActorContextResolver.class);
        membros = mock(MembroGrupoJpaRepository.class);
        controller = new FielController(mock(CriarFielUseCase.class), obter, listar,
                mock(AtualizarFielUseCase.class), mock(GerenciarCicloVidaFielUseCase.class), resolver, membros);
    }

    private void ator(String papel, UUID organizacaoId) {
        when(resolver.resolveRequired())
                .thenReturn(new EventoActorContext("ator", papel, "CLERO", organizacaoId, UUID.randomUUID(), null));
    }

    @Test
    void parocoComVinculoIgnoraEscopoOrganizacionalEVeTodos() {
        ator("paroco", orgId);

        controller.listar();

        verify(listar).execute();
        verify(listar, never()).executeNoGrupo(orgId);
    }

    @Test
    void adminComVinculoVeRegistroCompletoDeQualquerFielSemChecarGrupo() {
        ator("admin", orgId);
        UUID fielId = UUID.randomUUID();

        controller.obter(fielId);

        verify(obter).execute(fielId);
        verify(membros, never()).findByFielIdAndOrganizacaoIdAndAtivoTrue(fielId, orgId);
    }

    @Test
    void papelConsultivoRecebeSomenteIdENome() {
        ator("secretario-conselho", null);
        List<FielNomeResponse> nomes = List.of(new FielNomeResponse(UUID.randomUUID(), "Maria"));
        when(listar.executeNomes()).thenReturn(nomes);

        assertThat(controller.listar()).isEqualTo(nomes);
        verify(listar, never()).execute();
    }

    @Test
    void vigarioRecebeSomenteIdENomeNaConsultaIndividual() {
        ator("vigario", null);
        UUID fielId = UUID.randomUUID();
        FielNomeResponse nome = new FielNomeResponse(fielId, "Maria");
        when(obter.executeNome(fielId)).thenReturn(nome);

        assertThat(controller.obter(fielId)).isEqualTo(nome);
        verify(obter, never()).execute(fielId);
    }

    @Test
    void membroDeConselhoComGrupoSoVeNomesDoProprioGrupo() {
        ator("membro-conselho-pastoral", orgId);

        controller.listar();

        verify(listar).executeNomesNoGrupo(orgId);
    }

    @Test
    void coordenadorVeContatoDoProprioGrupoEEstaNegadoParaForaDele() {
        ator("coordenador", orgId);
        UUID fielId = UUID.randomUUID();

        controller.listar();
        verify(listar).executeContatoNoGrupo(orgId);

        assertThatThrownBy(() -> controller.obter(fielId)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void coordenadorSemGrupoNaoRecebeNenhumFiel() {
        ator("coordenador", null);

        assertThat(controller.listar()).isEmpty();
        verify(listar, never()).execute();
    }
}
