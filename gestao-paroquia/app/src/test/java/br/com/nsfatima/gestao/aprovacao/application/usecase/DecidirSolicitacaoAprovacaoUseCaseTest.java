package br.com.nsfatima.gestao.aprovacao.application.usecase;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecidirAprovacaoRequest;
import br.com.nsfatima.gestao.aprovacao.api.v1.dto.DecisaoAprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.application.port.out.AprovacaoMetricsPublisher;
import br.com.nsfatima.gestao.aprovacao.application.service.AprovacaoRecalculoConflitoService;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoNaoEncontradaException;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper.AprovacaoMapper;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.application.service.DetectorConflitoEventoService;
import br.com.nsfatima.gestao.calendario.domain.exception.ViolacaoRegraNegocioException;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecidirSolicitacaoAprovacaoUseCaseTest {

    @Mock
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    private AprovacaoMapper aprovacaoMapper;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    @Mock
    private ValidarAprovacaoUseCase validarAprovacaoUseCase;

    @Mock
    private AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    @Mock
    private AprovacaoAuditPublisher auditPublisher;

    @Mock
    private AprovacaoMetricsPublisher metricsPublisher;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private EstrategiaExecucaoAprovacao strategy;

    @Mock
    private DetectorConflitoEventoService detectorConflitoEventoService;

    @Mock
    private AprovacaoRecalculoConflitoService recalculoConflitoService;

    private DecidirSolicitacaoAprovacaoUseCase useCase;

    private UUID aprovacaoId;
    private AprovacaoEntity entity;
    private EventoActorContext actorContext;

    @BeforeEach
    void setUp() {
        aprovacaoMapper = new AprovacaoMapper(mock(LegacyEnumInconsistencyPublisher.class));

        useCase = new DecidirSolicitacaoAprovacaoUseCase(
                aprovacaoJpaRepository,
                aprovacaoMapper,
                actorContextResolver,
                validarAprovacaoUseCase,
                aprovacaoAcaoPayloadMapper,
                auditPublisher,
                metricsPublisher,
                List.of(strategy),
                eventPublisher,
                detectorConflitoEventoService,
                recalculoConflitoService
        );

        aprovacaoId = UUID.randomUUID();
        entity = new AprovacaoEntity();
        entity.setId(aprovacaoId);
        entity.setStatus(AprovacaoStatus.PENDENTE);
        entity.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        entity.setAprovadorPapel("COORD_CONSELHO");
        entity.setSolicitanteId("admin");
        entity.setActionPayloadJson(null);

        actorContext = new EventoActorContext("admin", "admin", "admin", UUID.randomUUID(), UUID.randomUUID(), null);
    }

    private void stubBasicApproval() {
        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.of(entity));
        when(actorContextResolver.resolveRequired()).thenReturn(actorContext);
    }

    @Test
    @DisplayName("Deve lançar AprovacaoNaoEncontradaException quando id não existir")
    void deveLancarExcecaoQuandoAprovacaoNaoEncontrada() {
        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.empty());

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs");

        assertThatThrownBy(() -> useCase.decidir(aprovacaoId, request))
                .isInstanceOf(AprovacaoNaoEncontradaException.class);
    }

    @Test
    @DisplayName("Deve aprovar CRIACAO_EVENTO com sucesso, registrar métricas e recalcular conflitos")
    void deveAprovarCriacaoEventoComSucesso() {
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(any(), any())).thenReturn(
                new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", UUID.randomUUID(), "CONFIRMADO", null));

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "Autorizo");
        DecisaoAprovacaoResponse response = useCase.decidir(aprovacaoId, request);

        assertThat(response.status()).isEqualTo("APROVADA");
        verify(auditPublisher).publishAprovacaoDecision(eq("admin"), eq(entity), eq("executado"), any());
        verify(metricsPublisher).publishAprovacaoFlow("CRIACAO_EVENTO", "EXECUTED");
        verify(metricsPublisher).recordAprovacao("CRIACAO_EVENTO");
        verify(metricsPublisher, never()).recordDelecao(anyString(), anyString());
        verify(metricsPublisher, never()).recordEdicao(anyString(), anyString());
        verify(recalculoConflitoService).recalcularConflitosPendentes(aprovacaoId);
        verify(eventPublisher).publishEvent(any(Object.class));
    }

    @Test
    @DisplayName("Deve registrar métrica de deleção ao aprovar CANCELAMENTO com sucesso")
    void deveRegistrarDelecaoAoAprovarCancelamento() {
        entity.setTipoSolicitacao(TipoSolicitacaoInput.CANCELAMENTO.name());
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CANCELAMENTO)).thenReturn(true);
        when(strategy.execute(any(), any())).thenReturn(
                new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", UUID.randomUUID(), "CANCELADO", null));

        useCase.decidir(aprovacaoId, new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs"));

        verify(metricsPublisher).recordDelecao("EVENTO", "APROVACAO");
    }

    @Test
    @DisplayName("Deve registrar métrica de edição ao aprovar ALTERACAO_HORARIO com sucesso")
    void deveRegistrarEdicaoAoAprovarAlteracaoHorario() {
        entity.setTipoSolicitacao(TipoSolicitacaoInput.ALTERACAO_HORARIO.name());
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.ALTERACAO_HORARIO)).thenReturn(true);
        when(strategy.execute(any(), any())).thenReturn(
                new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", UUID.randomUUID(), "CONFIRMADO", null));

        useCase.decidir(aprovacaoId, new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs"));

        verify(metricsPublisher).recordEdicao("EVENTO", "APROVACAO");
    }

    @Test
    @DisplayName("Deve lançar erro quando não houver estratégia para o tipo de solicitação")
    void deveLancarErroQuandoNaoHouverEstrategiaSuportada() {
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(false);

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs");

        assertThatThrownBy(() -> useCase.decidir(aprovacaoId, request))
                .hasMessageContaining("Unsupported aprovacao type");
    }

    @Test
    @DisplayName("Falha de execução por ViolacaoRegraNegocioException deve classificar como NEGOCIO_CADASTRAL")
    void deveClassificarFalhaComoNegocioCadastral() {
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(any(), any()))
                .thenThrow(new ViolacaoRegraNegocioException("Conflito de espaço físico detectado"));

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs");

        assertThatThrownBy(() -> useCase.decidir(aprovacaoId, request))
                .hasMessageContaining("Falha na execução");

        assertThat(entity.getStatus()).isEqualTo(AprovacaoStatus.FALHA_EXECUCAO);
        assertThat(entity.getTipoFalhaExecucao().name()).isEqualTo("NEGOCIO_CADASTRAL");
        verify(metricsPublisher).recordFalhaExecucao("CRIACAO_EVENTO", "NEGOCIO_CADASTRAL");
        verify(auditPublisher).publishAprovacaoDecision(eq("admin"), eq(entity), eq("falhou"), any());
    }

    @Test
    @DisplayName("Falha de execução por RuntimeException genérica deve classificar como SISTEMICA_APLICACAO")
    void deveClassificarFalhaComoSistemicaAplicacao() {
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(any(), any()))
                .thenThrow(new IllegalStateException("timeout de banco de dados"));

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs");

        assertThatThrownBy(() -> useCase.decidir(aprovacaoId, request))
                .hasMessageContaining("Falha na execução");

        assertThat(entity.getStatus()).isEqualTo(AprovacaoStatus.FALHA_EXECUCAO);
        assertThat(entity.getTipoFalhaExecucao().name()).isEqualTo("SISTEMICA_APLICACAO");
        assertThat(entity.getDetalheErroExecucao()).contains("IllegalStateException");
        verify(metricsPublisher).recordFalhaExecucao("CRIACAO_EVENTO", "SISTEMICA_APLICACAO");
    }

    @Test
    @DisplayName("Deve reprovar solicitação pontual, publicar métricas e acionar onRejected da estratégia")
    void deveReprovarSolicitacaoPontual() {
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.fetchResourceStatus(entity)).thenReturn("CANCELADO");

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.REPROVADA, "Sem espaço");
        DecisaoAprovacaoResponse response = useCase.decidir(aprovacaoId, request);

        assertThat(response.status()).isEqualTo("REPROVADA");
        assertThat(response.actionExecution().outcome()).isEqualTo("REJECTED");
        verify(strategy).onRejected(entity);
        verify(metricsPublisher).recordRejeicao("CRIACAO_EVENTO");
        verify(auditPublisher).publishAprovacaoDecision(eq("admin"), eq(entity), eq("reprovado"), any());
    }

    @Test
    @DisplayName("Deve considerar início da ação vindo do payload para bloquear decisão de evento expirado")
    void deveBloquearDecisaoDeEventoExpirado() {
        entity.setActionPayloadJson("{\"inicio\":\"2020-01-01T00:00:00Z\"}");
        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.of(entity));
        when(aprovacaoAcaoPayloadMapper.toPayload(entity.getActionPayloadJson()))
                .thenReturn(new AprovacaoAcaoPayload(
                        "idemp", null, "Título", "desc", null, UUID.randomUUID(), null,
                        Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2020-01-01T01:00:00Z"),
                        null, null, null, null, List.of(), null, UUID.randomUUID(), null, null, List.of(), null));

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs");

        assertThatThrownBy(() -> useCase.decidir(aprovacaoId, request))
                .hasMessageContaining("expired");

        verify(actorContextResolver, never()).resolveRequired();
    }

    // ---- Séries recorrentes (master/filhas) ----

    @Test
    @DisplayName("Master recorrente sem ocorrências filhas pendentes não pode ser reprovada em bloco")
    void deveBloquearReprovacaoDeMasterSemFilhasPendentes() {
        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.of(entity));
        when(actorContextResolver.resolveRequired()).thenReturn(actorContext);
        when(aprovacaoJpaRepository.countByAprovacaoPaiId(aprovacaoId)).thenReturn(2L);
        when(aprovacaoJpaRepository.findByAprovacaoPaiIdAndStatus(aprovacaoId, AprovacaoStatus.PENDENTE))
                .thenReturn(List.of());

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.REPROVADA, "obs");

        assertThatThrownBy(() -> useCase.decidir(aprovacaoId, request))
                .isInstanceOf(ViolacaoRegraNegocioException.class)
                .hasMessageContaining("já foram deliberadas");
    }

    @Test
    @DisplayName("Master recorrente aprovada deve cascatear aprovação para filhas pendentes, com sucesso e falha")
    void deveCascatearAprovacaoParaFilhasComSucessoEFalha() {
        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.of(entity));
        when(actorContextResolver.resolveRequired()).thenReturn(actorContext);
        when(aprovacaoJpaRepository.countByAprovacaoPaiId(aprovacaoId)).thenReturn(2L);

        AprovacaoEntity filhaOk = new AprovacaoEntity();
        filhaOk.setId(UUID.randomUUID());
        filhaOk.setStatus(AprovacaoStatus.PENDENTE);
        filhaOk.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        filhaOk.setAprovadorPapel("COORD_CONSELHO");
        filhaOk.setAprovacaoPaiId(aprovacaoId);

        AprovacaoEntity filhaFalha = new AprovacaoEntity();
        filhaFalha.setId(UUID.randomUUID());
        filhaFalha.setStatus(AprovacaoStatus.PENDENTE);
        filhaFalha.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        filhaFalha.setAprovadorPapel("COORD_CONSELHO");
        filhaFalha.setAprovacaoPaiId(aprovacaoId);

        when(aprovacaoJpaRepository.findByAprovacaoPaiIdAndStatus(aprovacaoId, AprovacaoStatus.PENDENTE))
                .thenReturn(List.of(filhaOk, filhaFalha));
        when(aprovacaoJpaRepository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(aprovacaoId))
                .thenReturn(List.of(filhaOk, filhaFalha));
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(eq(filhaOk), any())).thenReturn(
                new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", UUID.randomUUID(), "CONFIRMADO", null));
        when(strategy.execute(eq(filhaFalha), any())).thenThrow(new IllegalStateException("falha ao criar evento"));

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "Autorizo série");
        DecisaoAprovacaoResponse response = useCase.decidir(aprovacaoId, request);

        assertThat(response.status()).isEqualTo("APROVADA");
        assertThat(filhaOk.getStatus()).isEqualTo(AprovacaoStatus.APROVADA);
        assertThat(filhaFalha.getStatus()).isEqualTo(AprovacaoStatus.FALHA_EXECUCAO);
        assertThat(filhaFalha.getTipoFalhaExecucao().name()).isEqualTo("SISTEMICA_APLICACAO");
        verify(aprovacaoJpaRepository).save(filhaOk);
        verify(aprovacaoJpaRepository).save(filhaFalha);
        verify(recalculoConflitoService).recalcularConflitosPendentes(aprovacaoId);
    }

    @Test
    @DisplayName("Master recorrente reprovada deve cascatear reprovação para todas as filhas pendentes")
    void deveCascatearReprovacaoParaFilhasPendentes() {
        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.of(entity));
        when(actorContextResolver.resolveRequired()).thenReturn(actorContext);
        when(aprovacaoJpaRepository.countByAprovacaoPaiId(aprovacaoId)).thenReturn(1L);

        AprovacaoEntity filha = new AprovacaoEntity();
        filha.setId(UUID.randomUUID());
        filha.setStatus(AprovacaoStatus.PENDENTE);
        filha.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        filha.setAprovadorPapel("COORD_CONSELHO");
        filha.setAprovacaoPaiId(aprovacaoId);

        when(aprovacaoJpaRepository.findByAprovacaoPaiIdAndStatus(aprovacaoId, AprovacaoStatus.PENDENTE))
                .thenReturn(List.of(filha));
        when(aprovacaoJpaRepository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(aprovacaoId))
                .thenReturn(List.of(filha));
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.fetchResourceStatus(entity)).thenReturn(null);

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.REPROVADA, "Série cancelada");
        DecisaoAprovacaoResponse response = useCase.decidir(aprovacaoId, request);

        assertThat(response.status()).isEqualTo("REPROVADA");
        assertThat(filha.getStatus()).isEqualTo(AprovacaoStatus.REPROVADA);
        verify(aprovacaoJpaRepository).save(filha);
    }

    @Test
    @DisplayName("Deve consolidar pai automaticamente como aprovada quando última filha pontual for decidida")
    void deveConsolidarPaiComoAprovadaQuandoUltimaFilhaDecidida() {
        UUID paiId = UUID.randomUUID();
        entity.setAprovacaoPaiId(paiId);
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(any(), any())).thenReturn(
                new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", UUID.randomUUID(), "CONFIRMADO", null));

        AprovacaoEntity irmaAprovada = new AprovacaoEntity();
        irmaAprovada.setId(UUID.randomUUID());
        irmaAprovada.setStatus(AprovacaoStatus.APROVADA);
        irmaAprovada.setAprovacaoPaiId(paiId);

        AprovacaoEntity pai = new AprovacaoEntity();
        pai.setId(paiId);
        pai.setStatus(AprovacaoStatus.PENDENTE);
        pai.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        pai.setAprovadorPapel("COORD_CONSELHO");

        when(aprovacaoJpaRepository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(paiId))
                .thenReturn(List.of(entity, irmaAprovada));
        when(aprovacaoJpaRepository.findById(paiId)).thenReturn(Optional.of(pai));

        useCase.decidir(aprovacaoId, new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs"));

        assertThat(pai.getStatus()).isEqualTo(AprovacaoStatus.APROVADA);
        verify(aprovacaoJpaRepository).save(pai);
    }

    @Test
    @DisplayName("Deve consolidar pai automaticamente como reprovado quando todas as filhas forem reprovadas")
    void deveConsolidarPaiComoReprovadoQuandoTodasFilhasReprovadas() {
        UUID paiId = UUID.randomUUID();
        entity.setAprovacaoPaiId(paiId);
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.fetchResourceStatus(entity)).thenReturn(null);

        AprovacaoEntity irmaReprovada = new AprovacaoEntity();
        irmaReprovada.setId(UUID.randomUUID());
        irmaReprovada.setStatus(AprovacaoStatus.REPROVADA);
        irmaReprovada.setAprovacaoPaiId(paiId);

        AprovacaoEntity pai = new AprovacaoEntity();
        pai.setId(paiId);
        pai.setStatus(AprovacaoStatus.PENDENTE);
        pai.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        pai.setAprovadorPapel("COORD_CONSELHO");

        when(aprovacaoJpaRepository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(paiId))
                .thenReturn(List.of(entity, irmaReprovada));
        when(aprovacaoJpaRepository.findById(paiId)).thenReturn(Optional.of(pai));

        useCase.decidir(aprovacaoId, new DecidirAprovacaoRequest(AprovacaoStatus.REPROVADA, "obs"));

        assertThat(pai.getStatus()).isEqualTo(AprovacaoStatus.REPROVADA);
    }

    @Test
    @DisplayName("Não deve consolidar pai enquanto ainda houver filhas pendentes")
    void naoDeveConsolidarPaiComFilhasAindaPendentes() {
        UUID paiId = UUID.randomUUID();
        entity.setAprovacaoPaiId(paiId);
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(any(), any())).thenReturn(
                new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", UUID.randomUUID(), "CONFIRMADO", null));

        AprovacaoEntity irmaPendente = new AprovacaoEntity();
        irmaPendente.setId(UUID.randomUUID());
        irmaPendente.setStatus(AprovacaoStatus.PENDENTE);
        irmaPendente.setAprovacaoPaiId(paiId);

        when(aprovacaoJpaRepository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(paiId))
                .thenReturn(List.of(entity, irmaPendente));

        useCase.decidir(aprovacaoId, new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs"));

        // findById(paiId) é chamado uma vez para enriquecer o payload da filha antes da checagem de
        // conflito; a consolidação do pai encerra cedo (há irmã pendente) sem buscar/salvar o pai.
        verify(aprovacaoJpaRepository, times(1)).findById(paiId);
        verify(aprovacaoJpaRepository, never()).save(argThat(e -> e.getId().equals(paiId)));
    }

    @Test
    @DisplayName("Deve mesclar payload da filha com dados do pai (grupo/categoria/local) quando ausentes na filha")
    void deveMesclarPayloadDaFilhaComDadosDoPai() {
        UUID paiId = UUID.randomUUID();
        entity.setAprovacaoPaiId(paiId);
        entity.setActionPayloadJson("{\"filha\":true}");
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(any(), any())).thenReturn(
                new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", UUID.randomUUID(), "CONFIRMADO", null));

        AprovacaoEntity pai = new AprovacaoEntity();
        pai.setId(paiId);
        pai.setActionPayloadJson("{\"pai\":true}");

        UUID grupoPaiId = UUID.randomUUID();
        AprovacaoAcaoPayload payloadFilha = new AprovacaoAcaoPayload(
                "idemp-filha", null, "Missa", "desc", null, null, null,
                Instant.parse("2027-01-01T10:00:00Z"), Instant.parse("2027-01-01T11:00:00Z"),
                null, null, null, null, List.of(), null, null, null, null, List.of(), null);
        AprovacaoAcaoPayload payloadPai = new AprovacaoAcaoPayload(
                "idemp-pai", null, "Missa (série)", "desc", null, grupoPaiId, null,
                Instant.parse("2027-01-01T10:00:00Z"), Instant.parse("2027-01-01T11:00:00Z"),
                null, null, null, null, List.of(), null, null, null, null, List.of(), null);

        when(aprovacaoJpaRepository.findById(paiId)).thenReturn(Optional.of(pai));
        when(aprovacaoAcaoPayloadMapper.toPayload("{\"filha\":true}")).thenReturn(payloadFilha);
        when(aprovacaoAcaoPayloadMapper.toPayload("{\"pai\":true}")).thenReturn(payloadPai);
        when(aprovacaoAcaoPayloadMapper.toJson(any())).thenReturn("{\"merged\":true}");
        when(aprovacaoJpaRepository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(paiId)).thenReturn(List.of());

        useCase.decidir(aprovacaoId, new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs"));

        assertThat(entity.getActionPayloadJson()).isEqualTo("{\"merged\":true}");
    }

    @Test
    @DisplayName("Enriquecimento de payload da filha deve ignorar silenciosamente falha de interpretação do payload do pai")
    void deveIgnorarFalhaDeInterpretacaoDoPayloadDoPai() {
        UUID paiId = UUID.randomUUID();
        entity.setAprovacaoPaiId(paiId);
        entity.setActionPayloadJson("payload-invalido");
        stubBasicApproval();
        when(strategy.supports(TipoSolicitacaoInput.CRIACAO_EVENTO)).thenReturn(true);
        when(strategy.execute(any(), any())).thenReturn(
                new DecisaoAprovacaoResponse.ActionExecution("EXECUTED", UUID.randomUUID(), "CONFIRMADO", null));

        AprovacaoEntity pai = new AprovacaoEntity();
        pai.setId(paiId);
        pai.setActionPayloadJson("{\"pai\":true}");

        when(aprovacaoJpaRepository.findById(paiId)).thenReturn(Optional.of(pai));
        when(aprovacaoAcaoPayloadMapper.toPayload("payload-invalido"))
                .thenThrow(new IllegalArgumentException("payload malformado"));
        when(aprovacaoJpaRepository.findByAprovacaoPaiIdOrderByDataOcorrenciaUtcAsc(paiId)).thenReturn(List.of());

        useCase.decidir(aprovacaoId, new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs"));

        assertThat(entity.getActionPayloadJson()).isEqualTo("payload-invalido");
    }

    @Test
    @DisplayName("Deve bloquear aprovação em lote da série quando qualquer filha tiver conflito de recurso")
    void deveBloquearAprovacaoEmLoteQuandoFilhaTemConflito() {
        when(aprovacaoJpaRepository.findById(aprovacaoId)).thenReturn(Optional.of(entity));
        when(actorContextResolver.resolveRequired()).thenReturn(actorContext);
        when(aprovacaoJpaRepository.countByAprovacaoPaiId(aprovacaoId)).thenReturn(1L);

        AprovacaoEntity filha = new AprovacaoEntity();
        filha.setId(UUID.randomUUID());
        filha.setStatus(AprovacaoStatus.PENDENTE);
        filha.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        filha.setAprovacaoPaiId(aprovacaoId);
        filha.setActionPayloadJson("{\"filha\":true}");

        when(aprovacaoJpaRepository.findByAprovacaoPaiIdAndStatus(aprovacaoId, AprovacaoStatus.PENDENTE))
                .thenReturn(List.of(filha));

        AprovacaoAcaoPayload payload = new AprovacaoAcaoPayload(
                "idemp", null, "Missa", "desc", null, UUID.randomUUID(), null,
                Instant.parse("2027-01-01T10:00:00Z"), Instant.parse("2027-01-01T11:00:00Z"),
                null, null, null, null, List.of(), UUID.randomUUID(), null, null, null, List.of(), null);
        when(aprovacaoAcaoPayloadMapper.toPayload("{\"filha\":true}")).thenReturn(payload);
        when(detectorConflitoEventoService.detectarConflitos(
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(List.of(new br.com.nsfatima.gestao.calendario.application.dto.ConflitoEventoItemDto(
                        "LOCAL", payload.localId(), "Salão", UUID.randomUUID(), "Missa Solene",
                        payload.inicio(), payload.fim(), "Padre", 1, 2, "ocupado")));

        DecidirAprovacaoRequest request = new DecidirAprovacaoRequest(AprovacaoStatus.APROVADA, "obs");

        assertThatThrownBy(() -> useCase.decidir(aprovacaoId, request))
                .isInstanceOf(ViolacaoRegraNegocioException.class);

        verify(strategy, never()).execute(any(), any());
    }
}
