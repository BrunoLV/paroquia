package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.EnviarParaManutencaoRequest;
import br.com.nsfatima.gestao.patrimonio.application.port.out.ConsultaReservaDeAtivoPort;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoConflitanteException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencao;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.CodigoTombamento;
import br.com.nsfatima.gestao.patrimonio.domain.model.ReservaImpeditiva;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoManutencaoRepository;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EnviarAtivoParaManutencaoUseCaseTest {

    @Mock private AtivoPatrimonialRepository ativoRepository;
    @Mock private AtivoManutencaoRepository manutencaoRepository;
    @Mock private ConsultaReservaDeAtivoPort consultaReservaPort;
    @Mock private EventoActorContextResolver actorContextResolver;
    @Mock private PatrimonioAuthorizationService authorizationService;
    @Mock private PatrimonioAuditPublisher auditPublisher;
    @Mock private ApplicationEventPublisher eventPublisher;

    private EnviarAtivoParaManutencaoUseCase useCase;
    private final UUID ativoId = UUID.randomUUID();
    private final Instant inicio = Instant.now().plus(1, ChronoUnit.DAYS);
    private final Instant fim = inicio.plus(2, ChronoUnit.DAYS);

    @BeforeEach
    void setUp() {
        useCase = new EnviarAtivoParaManutencaoUseCase(ativoRepository, manutencaoRepository, consultaReservaPort,
                actorContextResolver, authorizationService, auditPublisher, eventPublisher);
    }

    private AtivoPatrimonial ativoDisponivel() {
        return new AtivoPatrimonial(new AtivoPatrimonialId(ativoId), "Projetor", CodigoTombamento.de(2026, 1), null,
                AtivoCategoria.ELETRONICO, LocalDate.now(), BigDecimal.TEN, UUID.randomUUID(), true,
                AtivoStatus.DISPONIVEL, UUID.randomUUID(), Instant.now(), Instant.now(), null, 0L);
    }

    private void stubComum() {
        when(ativoRepository.findById(new AtivoPatrimonialId(ativoId))).thenReturn(Optional.of(ativoDisponivel()));
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("a", "admin", "CLERO", null, UUID.randomUUID(), null));
        doNothing().when(authorizationService).assertPodeGerirPatrimonio(any(), any());
        when(manutencaoRepository.existeEmAbertoPorAtivo(any())).thenReturn(false);
    }

    @Test
    void bloqueiaQuandoHaReservaImpeditiva() {
        stubComum();
        when(consultaReservaPort.reservasImpeditivas(ativoId, inicio, fim)).thenReturn(List.of(
                new ReservaImpeditiva(UUID.randomUUID(), "Missa", inicio, fim, "CONFIRMADA", "Maria")));

        assertThatThrownBy(() -> useCase.execute(ativoId,
                new EnviarParaManutencaoRequest("reparo completo", inicio, fim)))
                .isInstanceOf(AtivoManutencaoConflitanteException.class);

        verify(manutencaoRepository, never()).save(any());
    }

    @Test
    void enviaParaManutencaoQuandoSemConflito() {
        stubComum();
        when(consultaReservaPort.reservasImpeditivas(ativoId, inicio, fim)).thenReturn(List.of());
        when(manutencaoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = useCase.execute(ativoId, new EnviarParaManutencaoRequest("reparo completo", inicio, fim));

        assertThat(response.dataInicio()).isEqualTo(inicio);
        verify(ativoRepository).save(any(AtivoPatrimonial.class));
        verify(manutencaoRepository).save(any(AtivoManutencao.class));
    }
}
