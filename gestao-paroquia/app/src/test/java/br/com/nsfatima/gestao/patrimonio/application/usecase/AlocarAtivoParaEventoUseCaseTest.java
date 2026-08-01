package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.aprovacao.application.mapper.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AlocacaoAtivoApprovalPendingResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AlocarAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoOperationResult;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.application.usecase.observacao.CreateNotaAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.domain.event.AlocacaoAtivoPendenteEvent;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoAlocacaoConflitoPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoVinculacaoPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AlocarAtivoParaEventoUseCaseTest {

    @Mock
    private AtivoPatrimonialJpaRepository ativoRepository;

    @Mock
    private EventoJpaRepository eventoRepository;

    @Mock
    private LocalJpaRepository localRepository;

    @Mock
    private AtivoAlocacaoJpaRepository alocacaoRepository;

    @Mock
    private AprovacaoJpaRepository aprovacaoRepository;

    @Mock
    private AtivoAlocacaoConflitoPolicy conflitoPolicy;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    @Mock
    private CreateNotaAtivoPatrimonialUseCase createNotaUseCase;

    @Mock
    private PatrimonioAuditPublisher auditPublisher;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AlocarAtivoParaEventoUseCase useCase;
    private UUID ativoId;
    private UUID eventoId;
    private UUID localId;
    private UUID organizacaoId;
    private final LocalDate dataInicio = LocalDate.now().plusDays(1);
    private final LocalDate dataFim = LocalDate.now().plusDays(3);

    @BeforeEach
    void setUp() {
        ApprovalActionPayloadMapper payloadMapper = new ApprovalActionPayloadMapper(
                new tools.jackson.databind.ObjectMapper());
        useCase = new AlocarAtivoParaEventoUseCase(ativoRepository, eventoRepository, localRepository,
                alocacaoRepository, aprovacaoRepository, actorContextResolver,
                new PatrimonioAuthorizationService(), new AtivoVinculacaoPolicy(conflitoPolicy),
                createNotaUseCase, auditPublisher, payloadMapper, eventPublisher);
        ativoId = UUID.randomUUID();
        eventoId = UUID.randomUUID();
        localId = UUID.randomUUID();
        organizacaoId = UUID.randomUUID();

        lenient().when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "admin", "CLERO", null, UUID.randomUUID(), null));
        lenient().when(ativoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(alocacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(aprovacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deveConfirmarImediatamenteQuandoOrganizacoesCoincidem() {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, organizacaoId);
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(criarEvento()));
        when(localRepository.findById(localId)).thenReturn(Optional.of(criarLocal(organizacaoId)));

        AtivoOperationResult result = useCase.execute(ativoId,
                new AlocarAtivoRequest(eventoId, "Utilização para exibição de slides", dataInicio, dataFim));

        assertThat(result).isInstanceOf(AtivoOperationResult.Success.class);
        assertThat(result.status()).isEqualTo(HttpStatus.OK);
        assertThat(((AtivoPatrimonialResponse) result.body()).status()).isEqualTo(AtivoStatus.ALOCADO);
        verify(createNotaUseCase).execute(eq(ativoId), any(), any(), any());
        verify(auditPublisher).publishAlocacao(any(), any(), any(), any());
        verify(aprovacaoRepository, never()).save(any());

        ArgumentCaptor<AtivoAlocacaoEntity> captor = ArgumentCaptor.forClass(AtivoAlocacaoEntity.class);
        verify(alocacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(AtivoAlocacaoStatus.CONFIRMADA);
        assertThat(captor.getValue().isEmprestimo()).isFalse();
    }

    @Test
    void deveConfirmarReservaSemAtrelarEventoQuandoEventoIdENulo() {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, organizacaoId);
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));

        AtivoOperationResult result = useCase.execute(ativoId,
                new AlocarAtivoRequest(null, "Reserva para uso futuro sem evento definido", dataInicio, dataFim));

        assertThat(result).isInstanceOf(AtivoOperationResult.Success.class);
        assertThat(result.status()).isEqualTo(HttpStatus.OK);
        assertThat(((AtivoPatrimonialResponse) result.body()).status()).isEqualTo(AtivoStatus.ALOCADO);
        verify(eventoRepository, never()).findById(any());
        verify(localRepository, never()).findById(any());
        verify(aprovacaoRepository, never()).save(any());

        ArgumentCaptor<AtivoAlocacaoEntity> captor = ArgumentCaptor.forClass(AtivoAlocacaoEntity.class);
        verify(alocacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getEventoId()).isNull();
        assertThat(captor.getValue().getStatus()).isEqualTo(AtivoAlocacaoStatus.CONFIRMADA);
        assertThat(captor.getValue().isEmprestimo()).isFalse();
    }

    @Test
    void deveCriarPendenciaDeAprovacaoQuandoOrganizacoesDivergem() {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, organizacaoId);
        UUID outraOrganizacaoId = UUID.randomUUID();
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(criarEvento()));
        when(localRepository.findById(localId)).thenReturn(Optional.of(criarLocal(outraOrganizacaoId)));

        AtivoOperationResult result = useCase.execute(ativoId,
                new AlocarAtivoRequest(eventoId, "Utilização para exibição de slides", dataInicio, dataFim));

        assertThat(result).isInstanceOf(AtivoOperationResult.Pending.class);
        assertThat(result.status()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(((AlocacaoAtivoApprovalPendingResponse) result.body()).ativoId()).isEqualTo(ativoId);
        assertThat(ativo.getStatus()).isEqualTo(AtivoStatus.DISPONIVEL);
        verify(ativoRepository, never()).save(any());
        verify(createNotaUseCase, never()).execute(any(), any(), any(), any());
        verify(eventPublisher).publishEvent(any(AlocacaoAtivoPendenteEvent.class));

        ArgumentCaptor<AtivoAlocacaoEntity> alocacaoCaptor = ArgumentCaptor.forClass(AtivoAlocacaoEntity.class);
        verify(alocacaoRepository, org.mockito.Mockito.atLeastOnce()).save(alocacaoCaptor.capture());
        assertThat(alocacaoCaptor.getValue().getStatus()).isEqualTo(AtivoAlocacaoStatus.PENDENTE_APROVACAO);

        ArgumentCaptor<AprovacaoEntity> aprovacaoCaptor = ArgumentCaptor.forClass(AprovacaoEntity.class);
        verify(aprovacaoRepository).save(aprovacaoCaptor.capture());
        assertThat(aprovacaoCaptor.getValue().getStatusEnum()).isEqualTo(AprovacaoStatus.PENDENTE);
    }

    @Test
    void deveConfirmarAlocacaoAoAprovarSolicitacaoPendente() {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, organizacaoId);
        UUID aprovacaoId = UUID.randomUUID();
        AtivoAlocacaoEntity ocupacaoPendente = new AtivoAlocacaoEntity();
        ocupacaoPendente.setId(UUID.randomUUID());
        ocupacaoPendente.setAtivoId(ativoId);
        ocupacaoPendente.setEventoId(eventoId);
        ocupacaoPendente.setDataInicio(dataInicio);
        ocupacaoPendente.setDataFim(dataFim);
        ocupacaoPendente.setJustificativa("Utilização para exibição de slides");
        ocupacaoPendente.setStatus(AtivoAlocacaoStatus.PENDENTE_APROVACAO);

        when(alocacaoRepository.findByAprovacaoId(aprovacaoId)).thenReturn(Optional.of(ocupacaoPendente));
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));

        AtivoPatrimonialResponse response = useCase.executeApprovedAlocacao(aprovacaoId, "coordenador",
                UUID.randomUUID());

        assertThat(response.status()).isEqualTo(AtivoStatus.ALOCADO);
        assertThat(ocupacaoPendente.getStatus()).isEqualTo(AtivoAlocacaoStatus.CONFIRMADA);
        verify(createNotaUseCase).execute(eq(ativoId), any(), any(), any());
    }

    @Test
    void deveRejeitarAlocacaoDeAtivoBaixado() {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.BAIXADO, organizacaoId);
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));

        assertThatThrownBy(() -> useCase.execute(ativoId,
                new AlocarAtivoRequest(eventoId, "Utilização para exibição de slides", dataInicio, dataFim)))
                .isInstanceOf(AtivoAlocacaoInvalidaException.class);
    }

    private AtivoPatrimonialEntity criarAtivo(AtivoStatus status, UUID organizacaoId) {
        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(ativoId);
        ativo.setNome("Projetor");
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(status);
        ativo.setOrganizacaoId(organizacaoId);
        return ativo;
    }

    private EventoEntity criarEvento() {
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setLocalId(localId);
        return evento;
    }

    private LocalEntity criarLocal(UUID organizacaoId) {
        LocalEntity local = new LocalEntity();
        local.setId(localId);
        local.setOrganizacaoId(organizacaoId);
        return local;
    }
}
