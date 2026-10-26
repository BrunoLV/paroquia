package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ListarAprovacoesUseCaseTest {

    private static final Pageable PAGINA = PageRequest.of(0, 10);

    private AprovacaoJpaRepository repository;
    private EventoActorContextResolver actorResolver;
    private ListarAprovacoesUseCase useCase;
    private final UUID usuarioId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = mock(AprovacaoJpaRepository.class);
        actorResolver = mock(EventoActorContextResolver.class);
        AprovacaoMapper mapper = new AprovacaoMapper(mock(LegacyEnumInconsistencyPublisher.class));
        useCase = new ListarAprovacoesUseCase(repository, mapper, actorResolver,
                new ValidarAprovacaoUseCase(repository));
    }

    private void autenticarComo(String papel, UUID id) {
        when(actorResolver.resolveRequired())
                .thenReturn(new EventoActorContext("ator", papel, "CLERO", null, id, null));
    }

    private AprovacaoEntity aprovacao(String solicitanteId) {
        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setStatus(AprovacaoStatus.FALHA_EXECUCAO);
        entity.setTipoSolicitacao("CRIACAO_EVENTO");
        entity.setSolicitanteId(solicitanteId);
        entity.setActionPayloadJson("{\"titulo\":\"x\"}");
        entity.setDetalheErroExecucao("java.lang.IllegalStateException: stack");
        return entity;
    }

    @Test
    void parocoVeTodasAsAprovacoesComDetalheTecnico() {
        autenticarComo("paroco", usuarioId);
        when(repository.findByFilterDoSolicitante(any(), any(), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(aprovacao("outro"))));

        AprovacaoResponse resposta = useCase.execute(null, null, PAGINA).getContent().get(0);

        assertThat(resposta.detalheErroExecucao()).isNotNull();
        assertThat(resposta.actionPayloadJson()).isNotNull();
    }

    @Test
    void solicitanteVeSomenteAsPropriasSemDetalheTecnico() {
        autenticarComo("coordenador", usuarioId);
        when(repository.findByFilterDoSolicitante(any(), any(), eq(usuarioId.toString()),
                any())).thenReturn(new PageImpl<>(List.of(aprovacao(usuarioId.toString()))));

        AprovacaoResponse resposta = useCase.execute(null, null, PAGINA).getContent().get(0);

        assertThat(resposta.detalheErroExecucao()).isNull();
        assertThat(resposta.actionPayloadJson()).isNotNull();
    }

    @Test
    void adminNaoDecideEPortantoVeSomenteAsPropriasAprovacoes() {
        autenticarComo("admin", usuarioId);
        when(repository.findByFilterDoSolicitante(any(), any(), eq(usuarioId.toString()),
                any())).thenReturn(new PageImpl<>(List.of()));

        assertThat(useCase.execute(null, null, PAGINA).getContent()).isEmpty();
    }

    @Test
    void atorSemUsuarioIdentificadoEhNegado() {
        autenticarComo("coordenador", null);

        assertThatThrownBy(() -> useCase.execute(null, null, PAGINA)).isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findByFilterDoSolicitante(any(), any(), any(), any());
    }

    @Test
    void naoSolicitanteNaoListaOcorrenciasDeSerieAlheia() {
        UUID paiId = UUID.randomUUID();
        autenticarComo("coordenador", usuarioId);
        when(repository.findById(paiId)).thenReturn(Optional.of(aprovacao("outro")));

        assertThatThrownBy(() -> useCase.listarOcorrencias(paiId)).isInstanceOf(AccessDeniedException.class);
        verify(repository, never()).findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(any());
    }

    @Test
    void solicitanteListaOcorrenciasDaPropriaSerieSemDetalheTecnico() {
        UUID paiId = UUID.randomUUID();
        autenticarComo("coordenador", usuarioId);
        when(repository.findById(paiId)).thenReturn(Optional.of(aprovacao(usuarioId.toString())));
        when(repository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(paiId))
                .thenReturn(List.of(aprovacao(usuarioId.toString())));

        List<AprovacaoResponse> ocorrencias = useCase.listarOcorrencias(paiId);

        assertThat(ocorrencias).hasSize(1);
        assertThat(ocorrencias.get(0).detalheErroExecucao()).isNull();
    }

    @Test
    void parocoListaOcorrenciasDeQualquerSerie() {
        UUID paiId = UUID.randomUUID();
        autenticarComo("bispo-diocesano", usuarioId);
        when(repository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(paiId))
                .thenReturn(List.of(aprovacao("outro")));

        assertThat(useCase.listarOcorrencias(paiId).get(0).detalheErroExecucao()).isNotNull();
    }
}
