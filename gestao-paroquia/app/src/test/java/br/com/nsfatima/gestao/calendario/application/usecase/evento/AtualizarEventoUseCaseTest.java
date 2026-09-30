package br.com.nsfatima.gestao.calendario.application.usecase.evento;

import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.AtualizarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.ResultadoOperacaoEvento;
import br.com.nsfatima.gestao.calendario.application.service.EventoPatchAuthorizationService;
import br.com.nsfatima.gestao.calendario.application.service.ValidadorPrioridadeAgenda;
import br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.AtualizarEventoSolicitacaoAprovacaoUseCase;
import br.com.nsfatima.gestao.calendario.domain.exception.ViolacaoRegraNegocioException;
import br.com.nsfatima.gestao.calendario.domain.policy.ModoRequisicaoCriacao;
import br.com.nsfatima.gestao.calendario.domain.service.EventoDomainService;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.EventoStatusInput;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.infrastructure.config.CacheConfig;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoLocalEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.mapper.EventoMapper;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoLocalJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoRecorrenciaJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.projeto.application.service.ValidadorVinculoProjeto;
import br.com.nsfatima.gestao.reserva.application.usecase.EventoReservaIntegrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes focados nos ramos de {@link AtualizarEventoUseCase} não exercitados pelas suítes de
 * integração do fluxo de atualização de evento (ver pacote {@code calendario.integration.eventos}):
 * validação de justificativa do Secretário, liberação de conflito de agenda, sincronização de
 * locais legados, invalidação seletiva de cache de projeto e métricas de cancelamento.
 */
@ExtendWith(MockitoExtension.class)
class AtualizarEventoUseCaseTest {

    @Mock
    private EventoDomainService domainService;
    @Mock
    private EventoJpaRepository repository;
    @Mock
    private EventoRecorrenciaJpaRepository recurrenceRepository;
    @Mock
    private EventoEnvolvidoJpaRepository envolvimentoRepository;
    @Mock
    private EventoMapper mapper;
    @Mock
    private EventoPatchAuthorizationService authorizationService;
    @Mock
    private EventoActorContextResolver actorContextResolver;
    @Mock
    private AtualizarEventoEnvolvidosUseCase updateEnvolvidosUseCase;
    @Mock
    private LimparEventoEnvolvidosUseCase clearEnvolvidosUseCase;
    @Mock
    private ValidadorVinculoProjeto validadorVinculoProjeto;
    @Mock
    private CadastroEventoMetricsPublisher metricsPublisher;
    @Mock
    private AtualizarEventoSolicitacaoAprovacaoUseCase aprovacaoRequestUseCase;
    @Mock
    private EventoAuditPublisher auditPublisher;
    @Mock
    private ValidadorPrioridadeAgenda validadorPrioridadeAgendaService;
    @Mock
    private CacheManager cacheManager;
    @Mock
    private EventoReservaIntegrationService eventoReservaIntegrationService;
    @Mock
    private EventoLocalJpaRepository eventoLocalJpaRepository;

    private AtualizarEventoUseCase novoUseCase() {
        return new AtualizarEventoUseCase(domainService, repository, recurrenceRepository, envolvimentoRepository,
                mapper, authorizationService, actorContextResolver, updateEnvolvidosUseCase, clearEnvolvidosUseCase,
                validadorVinculoProjeto, metricsPublisher, aprovacaoRequestUseCase, auditPublisher,
                validadorPrioridadeAgendaService, cacheManager, eventoReservaIntegrationService,
                eventoLocalJpaRepository);
    }

    private EventoEntity novaEntidade() {
        EventoEntity entity = new EventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo("Missa Dominical");
        entity.setGrupoParoquialResponsavelId(UUID.randomUUID());
        entity.setInicioUtc(Instant.parse("2026-10-15T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-10-15T11:00:00Z"));
        entity.setStatus(EventoStatusInput.CONFIRMADO.name());
        entity.setFormato(FormatoEvento.ONLINE);
        entity.setCategoria(CategoriaEvento.PASTORAL);
        return entity;
    }

    /** Stubs mínimos comuns para que {@code performUpdate} conclua com sucesso. */
    private void stubHappyPathCollaborators(EventoEntity entity, AtualizarEventoRequest request) {
        lenient().when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        lenient().when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("padre-joao", "paroco", "PAROQUIA", null, null, null));
        lenient().when(authorizationService.resolveModoRequisicaoCriacao(any(), any(), any()))
                .thenReturn(ModoRequisicaoCriacao.IMEDIATO);
        lenient().when(repository.save(any(EventoEntity.class))).thenReturn(entity);
        lenient().when(envolvimentoRepository.findByEventoId(entity.getId())).thenReturn(List.of());
        lenient().when(validadorPrioridadeAgendaService.validarPrioridadeEConflitos(
                any(), any(), any(), any(), any(), any(), anyBoolean())).thenReturn(List.of());
    }

    @Test
    @DisplayName("Secretário sem justificativa não pode atualizar evento (campo sensível)")
    void secretarioSemJustificativaLancaExcecao() {
        EventoEntity entity = novaEntidade();
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                "Novo Título", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("secretaria-maria", "secretaria-paroquial", "PAROQUIA", null, null, null));

        assertThatThrownBy(() -> novoUseCase().execute(entity.getId(), request))
                .isInstanceOf(ViolacaoRegraNegocioException.class)
                .hasMessageContaining("Justificativa é obrigatória");
    }

    @Test
    @DisplayName("Secretário com motivo de cancelamento preenchido satisfaz a exigência de justificativa")
    void secretarioComMotivoCancelamentoPassaNaValidacao() {
        EventoEntity entity = novaEntidade();
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                null, null, null, null, null, EventoStatusInput.CANCELADO, null, "Local interditado",
                null, null, null, null, null, null, null, null, null, null, null);
        stubHappyPathCollaborators(entity, request);
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("secretaria-maria", "secretaria-paroquial", "PAROQUIA", null, null, null));

        novoUseCase().execute(entity.getId(), request);

        verify(authorizationService).assertCanEditGeneral(any(), any(), any());
    }

    @Test
    @DisplayName("liberarConflitoAgenda=true exige autorização específica do ator")
    void liberarConflitoAgendaExigeAutorizacaoEspecifica() {
        EventoEntity entity = novaEntidade();
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                "Novo Título", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, true, null, null, null);
        stubHappyPathCollaborators(entity, request);

        novoUseCase().execute(entity.getId(), request);

        verify(authorizationService).assertCanLiberarConflitoAgenda(any());
    }

    @Test
    @DisplayName("sem liberarConflitoAgenda não checa autorização de liberação")
    void semLiberarConflitoAgendaNaoChecaAutorizacaoEspecifica() {
        EventoEntity entity = novaEntidade();
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                "Novo Título", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);
        stubHappyPathCollaborators(entity, request);

        novoUseCase().execute(entity.getId(), request);

        verify(authorizationService, never()).assertCanLiberarConflitoAgenda(any());
    }

    @Test
    @DisplayName("localIds ausente mas evento sem locais migrados cria o registro legado a partir de localId")
    void semLocalIdsMigraLocalIdLegadoParaTabelaDeLocais() {
        EventoEntity entity = novaEntidade();
        UUID localId = UUID.randomUUID();
        entity.setLocalId(localId);
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                "Novo Título", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);
        stubHappyPathCollaborators(entity, request);
        when(eventoLocalJpaRepository.findByEventoId(entity.getId()))
                .thenReturn(List.of(), List.of(new EventoLocalEntity(entity.getId(), localId)));

        novoUseCase().execute(entity.getId(), request);

        verify(eventoLocalJpaRepository).save(any(EventoLocalEntity.class));
    }

    @Test
    @DisplayName("cancelamento de evento registra métrica de deleção, não de edição")
    void cancelamentoRegistraMetricaDeDelecao() {
        EventoEntity entity = novaEntidade();
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                null, null, null, null, null, EventoStatusInput.CANCELADO, null, "Chuva forte",
                null, null, null, null, null, null, null, null, null, null, null);
        stubHappyPathCollaborators(entity, request);

        novoUseCase().execute(entity.getId(), request);

        verify(metricsPublisher).recordDelecao("EVENTO", "DIRETA");
        verify(metricsPublisher, never()).recordEdicao(any(), any());
    }

    @Test
    @DisplayName("edição sem cancelamento registra métrica de edição, não de deleção")
    void edicaoSemCancelamentoRegistraMetricaDeEdicao() {
        EventoEntity entity = novaEntidade();
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                "Novo Título", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);
        stubHappyPathCollaborators(entity, request);

        novoUseCase().execute(entity.getId(), request);

        verify(metricsPublisher).recordEdicao("EVENTO", "DIRETA");
        verify(metricsPublisher, never()).recordDelecao(any(), any());
    }

    @Test
    @DisplayName("participantes vazio limpa envolvidos em vez de atualizar a lista")
    void participantesVazioLimpaEnvolvidos() {
        EventoEntity entity = novaEntidade();
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                null, null, null, null, null, null, null, null,
                null, List.of(), null, null, null, null, null, null, null, null, null);
        stubHappyPathCollaborators(entity, request);

        novoUseCase().execute(entity.getId(), request);

        verify(clearEnvolvidosUseCase).execute(entity.getId());
        verify(updateEnvolvidosUseCase, never()).execute(any(), any());
    }

    @Test
    @DisplayName("mudança de organização responsável evicta o cache de resumo de projeto (antigo e novo)")
    void mudancaDeProjetoEvictaCacheAntigoENovo() {
        EventoEntity entity = novaEntidade();
        UUID projetoAntigo = UUID.randomUUID();
        UUID projetoNovo = UUID.randomUUID();
        entity.setProjetoId(projetoAntigo);
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                "Novo Título", null, null, null, null, null, null, null,
                null, null, null, projetoNovo, null, null, null, null, null, null, null);
        stubHappyPathCollaborators(entity, request);

        Cache cache = mock(Cache.class);
        when(cacheManager.getCache(CacheConfig.PROJETO_RESUMO_CACHE)).thenReturn(cache);
        doAnswerSetProjeto(entity, projetoNovo);

        novoUseCase().execute(entity.getId(), request);

        verify(cache).evict(projetoAntigo);
        verify(cache).evict(projetoNovo);
    }

    private void doAnswerSetProjeto(EventoEntity entity, UUID projetoNovo) {
        doAnswer(inv -> {
            entity.setProjetoId(projetoNovo);
            return null;
        }).when(mapper).applyPatch(eq(entity), any(), any());
    }

    @Test
    @DisplayName("Corpo do PATCH vazio é rejeitado antes de buscar o evento")
    void payloadVazioLancaExcecaoSemBuscarEvento() {
        UUID eventoId = UUID.randomUUID();
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> novoUseCase().execute(eventoId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Corpo do PATCH não pode ser vazio");

        verify(repository, never()).findById(any());
    }

    @Test
    @DisplayName("PRESENCIAL sem locais informados nem existentes lança ViolacaoRegraNegocioException")
    void presencialSemLocaisLancaExcecao() {
        EventoEntity entity = novaEntidade();
        entity.setFormato(FormatoEvento.PRESENCIAL);
        entity.setLocalId(null);
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                "Novo Título", null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null);

        when(repository.findById(entity.getId())).thenReturn(Optional.of(entity));
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("padre-joao", "paroco", "PAROQUIA", null, null, null));

        assertThatThrownBy(() -> novoUseCase().execute(entity.getId(), request))
                .isInstanceOf(ViolacaoRegraNegocioException.class)
                .hasMessageContaining("Ao menos um local deve ser informado");
    }

    @Test
    @DisplayName("PRESENCIAL com localId informado no request satisfaz a exigência de local")
    void presencialComLocalIdNoRequestSatisfazExigencia() {
        EventoEntity entity = novaEntidade();
        entity.setFormato(FormatoEvento.PRESENCIAL);
        entity.setLocalId(null);
        UUID localId = UUID.randomUUID();
        AtualizarEventoRequest request = new AtualizarEventoRequest(
                "Novo Título", null, null, null, null, null, null, null,
                null, null, null, null, localId, null, null, null, null, null, null);
        stubHappyPathCollaborators(entity, request);
        when(eventoLocalJpaRepository.findByEventoId(entity.getId())).thenReturn(List.of());

        ResultadoOperacaoEvento resultado = novoUseCase().execute(entity.getId(), request);

        assertThat(resultado).isInstanceOf(ResultadoOperacaoEvento.Sucesso.class);
    }
}
