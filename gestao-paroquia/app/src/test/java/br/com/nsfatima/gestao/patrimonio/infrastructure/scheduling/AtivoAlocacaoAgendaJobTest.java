package br.com.nsfatima.gestao.patrimonio.infrastructure.scheduling;

import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.JobLockJpaRepository;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.domain.event.AtivoAlocacaoAtrasoEvent;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtivoAlocacaoAgendaJobTest {

    @Mock
    private AtivoAlocacaoJpaRepository alocacaoRepository;

    @Mock
    private AtivoPatrimonialJpaRepository ativoRepository;

    @Mock
    private JobLockJpaRepository lockRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private AtivoAlocacaoAgendaJob job;

    @BeforeEach
    void setUp() {
        job = new AtivoAlocacaoAgendaJob(alocacaoRepository, ativoRepository, lockRepository, eventPublisher);
        lenient().when(lockRepository.acquireLock(anyString(), any(), any())).thenReturn(true);
        lenient().when(alocacaoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(ativoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    private AtivoPatrimonialEntity ativoDisponivel(UUID id) {
        AtivoPatrimonialEntity ativo = new AtivoPatrimonialEntity();
        ativo.setId(id);
        ativo.setNome("Projetor");
        ativo.setCategoria(AtivoCategoria.ELETRONICO);
        ativo.setStatus(AtivoStatus.DISPONIVEL);
        ativo.setGrupoParoquialId(UUID.randomUUID());
        return ativo;
    }

    private AtivoAlocacaoEntity ocupacao(UUID ativoId, AtivoAlocacaoTipo tipo, LocalDate inicio, LocalDate fim,
                                         AtivoAlocacaoStatus status) {
        AtivoAlocacaoEntity ocupacao = new AtivoAlocacaoEntity();
        ocupacao.setId(UUID.randomUUID());
        ocupacao.setAtivoId(ativoId);
        ocupacao.setTipo(tipo);
        ocupacao.setDataInicio(inicio);
        ocupacao.setDataFim(fim);
        ocupacao.setStatus(status);
        ocupacao.setGrupoParoquialOrigemId(UUID.randomUUID());
        ocupacao.setCriadoEmUtc(Instant.now());
        return ocupacao;
    }

    @Test
    void deveAtivarReservaDeEventoCujoInicioChegou() {
        UUID ativoId = UUID.randomUUID();
        AtivoPatrimonialEntity ativo = ativoDisponivel(ativoId);
        AtivoAlocacaoEntity reserva = ocupacao(ativoId, AtivoAlocacaoTipo.ALOCACAO_EVENTO,
                LocalDate.now(), LocalDate.now().plusDays(2), AtivoAlocacaoStatus.CONFIRMADA);

        when(alocacaoRepository.findByStatusAndDataInicioLessThanEqual(AtivoAlocacaoStatus.CONFIRMADA, LocalDate.now()))
                .thenReturn(List.of(reserva));
        when(alocacaoRepository.findByStatusAndDataFimLessThanEqualAndAlertaAtrasoEmitidoEmIsNull(
                any(), any())).thenReturn(List.of());
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));

        job.executeInternal(null);

        assertThat(ativo.getStatus()).isEqualTo(AtivoStatus.ALOCADO);
    }

    @Test
    void deveAtivarManutencaoCujoInicioChegouComStatusEmManutencao() {
        UUID ativoId = UUID.randomUUID();
        AtivoPatrimonialEntity ativo = ativoDisponivel(ativoId);
        AtivoAlocacaoEntity manutencao = ocupacao(ativoId, AtivoAlocacaoTipo.MANUTENCAO,
                LocalDate.now(), LocalDate.now().plusDays(5), AtivoAlocacaoStatus.CONFIRMADA);

        when(alocacaoRepository.findByStatusAndDataInicioLessThanEqual(AtivoAlocacaoStatus.CONFIRMADA, LocalDate.now()))
                .thenReturn(List.of(manutencao));
        when(alocacaoRepository.findByStatusAndDataFimLessThanEqualAndAlertaAtrasoEmitidoEmIsNull(
                any(), any())).thenReturn(List.of());
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));

        job.executeInternal(null);

        assertThat(ativo.getStatus()).isEqualTo(AtivoStatus.EM_MANUTENCAO);
    }

    @Test
    void deveMarcarAtrasadaEPublicarEventoEImpactarReservaSeguinte() {
        UUID ativoId = UUID.randomUUID();
        AtivoAlocacaoEntity atrasada = ocupacao(ativoId, AtivoAlocacaoTipo.ALOCACAO_EVENTO,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(3), AtivoAlocacaoStatus.CONFIRMADA);
        AtivoAlocacaoEntity proximaReserva = ocupacao(ativoId, AtivoAlocacaoTipo.ALOCACAO_EVENTO,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(5), AtivoAlocacaoStatus.CONFIRMADA);

        when(alocacaoRepository.findByStatusAndDataInicioLessThanEqual(any(), any())).thenReturn(List.of());
        when(alocacaoRepository.findByStatusAndDataFimLessThanEqualAndAlertaAtrasoEmitidoEmIsNull(
                AtivoAlocacaoStatus.CONFIRMADA, LocalDate.now().minusDays(2)))
                .thenReturn(List.of(atrasada));
        when(alocacaoRepository.findByAtivoIdAndStatusIn(ativoId,
                List.of(AtivoAlocacaoStatus.PENDENTE_APROVACAO, AtivoAlocacaoStatus.CONFIRMADA)))
                .thenReturn(List.of(proximaReserva));

        job.executeInternal(null);

        assertThat(atrasada.getStatus()).isEqualTo(AtivoAlocacaoStatus.ATRASADA);
        assertThat(atrasada.getAlertaAtrasoEmitidoEm()).isNotNull();
        assertThat(proximaReserva.isImpactadaPorAtraso()).isTrue();
        verify(eventPublisher, times(1)).publishEvent(any(AtivoAlocacaoAtrasoEvent.class));
    }

    @Test
    void naoDeveExecutarQuandoLockJaEstiverAdquirido() {
        when(lockRepository.acquireLock(anyString(), any(), any())).thenReturn(false);

        job.executeInternal(null);

        verify(alocacaoRepository, never()).findByStatusAndDataInicioLessThanEqual(any(), any());
    }

    @Test
    void deveLiberarLockAoFinalizar() {
        when(alocacaoRepository.findByStatusAndDataInicioLessThanEqual(any(), any())).thenReturn(List.of());
        when(alocacaoRepository.findByStatusAndDataFimLessThanEqualAndAlertaAtrasoEmitidoEmIsNull(any(), any()))
                .thenReturn(List.of());

        job.executeInternal(null);

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        verify(lockRepository).releaseLock(nameCaptor.capture());
        assertThat(nameCaptor.getValue()).isEqualTo("ATIVO_ALOCACAO_AGENDA_JOB");
    }
}
