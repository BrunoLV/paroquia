package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.application.port.out.ConsultaReservaDeAtivoPort;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoExclusaoBloqueadaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.CodigoTombamento;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcluirAtivoPatrimonialUseCaseTest {

    @Mock private AtivoPatrimonialRepository ativoRepository;
    @Mock private AtivoManutencaoRepository manutencaoRepository;
    @Mock private ConsultaReservaDeAtivoPort consultaReservaPort;
    @Mock private EventoActorContextResolver actorContextResolver;
    @Mock private PatrimonioAuthorizationService authorizationService;
    @Mock private PatrimonioAuditPublisher auditPublisher;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ExcluirAtivoPatrimonialUseCase useCase;
    private final UUID ativoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ExcluirAtivoPatrimonialUseCase(ativoRepository, manutencaoRepository, consultaReservaPort,
                actorContextResolver, authorizationService, auditPublisher, eventPublisher);
        AtivoPatrimonial ativo = new AtivoPatrimonial(new AtivoPatrimonialId(ativoId), "Projetor",
                CodigoTombamento.de(2026, 1), null, AtivoCategoria.ELETRONICO, LocalDate.now(), BigDecimal.TEN,
                UUID.randomUUID(), true, AtivoStatus.DISPONIVEL, UUID.randomUUID(), Instant.now(), Instant.now(), null, 0L);
        when(ativoRepository.findById(new AtivoPatrimonialId(ativoId))).thenReturn(Optional.of(ativo));
        lenient().when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("a", "admin", "CLERO", null, UUID.randomUUID(), null));
    }

    @Test
    void bloqueiaComManutencaoEmAberto() {
        when(manutencaoRepository.existeEmAbertoPorAtivo(any())).thenReturn(true);
        assertThatThrownBy(() -> useCase.execute(ativoId)).isInstanceOf(AtivoExclusaoBloqueadaException.class);
        verify(ativoRepository, never()).save(any());
    }

    @Test
    void bloqueiaComReservaFutura() {
        when(manutencaoRepository.existeEmAbertoPorAtivo(any())).thenReturn(false);
        when(consultaReservaPort.temReservaFuturaImpeditiva(ativoId)).thenReturn(true);
        assertThatThrownBy(() -> useCase.execute(ativoId)).isInstanceOf(AtivoExclusaoBloqueadaException.class);
    }

    @Test
    void excluiQuandoLiberado() {
        when(manutencaoRepository.existeEmAbertoPorAtivo(any())).thenReturn(false);
        when(consultaReservaPort.temReservaFuturaImpeditiva(ativoId)).thenReturn(false);
        assertThatCode(() -> useCase.execute(ativoId)).doesNotThrowAnyException();
        verify(ativoRepository).save(any(AtivoPatrimonial.class));
        verify(auditPublisher).publishExclusao(any(), any(), any());
    }
}
