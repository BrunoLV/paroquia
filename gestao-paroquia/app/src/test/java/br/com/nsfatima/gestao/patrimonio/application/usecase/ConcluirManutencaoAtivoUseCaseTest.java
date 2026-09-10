package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencao;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencaoId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.CodigoTombamento;
import br.com.nsfatima.gestao.patrimonio.domain.model.PeriodoManutencao;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoManutencaoRepository;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConcluirManutencaoAtivoUseCaseTest {

    @Mock private AtivoPatrimonialRepository ativoRepository;
    @Mock private AtivoManutencaoRepository manutencaoRepository;
    @Mock private EventoActorContextResolver actorContextResolver;
    @Mock private PatrimonioAuthorizationService authorizationService;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ConcluirManutencaoAtivoUseCase useCase;
    private final UUID ativoId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ConcluirManutencaoAtivoUseCase(ativoRepository, manutencaoRepository, actorContextResolver,
                authorizationService, eventPublisher);
        AtivoPatrimonial ativo = new AtivoPatrimonial(new AtivoPatrimonialId(ativoId), "Projetor",
                CodigoTombamento.de(2026, 1), null, AtivoCategoria.ELETRONICO, LocalDate.now(), BigDecimal.TEN,
                UUID.randomUUID(), true, AtivoStatus.EM_MANUTENCAO, UUID.randomUUID(), Instant.now(), Instant.now(), null, 0L);
        when(ativoRepository.findById(new AtivoPatrimonialId(ativoId))).thenReturn(Optional.of(ativo));
        lenient().when(ativoRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("a", "admin", "CLERO", null, UUID.randomUUID(), null));
    }

    private AtivoManutencao emAberto() {
        Instant inicio = Instant.now().minus(2, ChronoUnit.DAYS);
        return AtivoManutencao.registrar(AtivoManutencaoId.generate(), new AtivoPatrimonialId(ativoId), "reparo",
                new PeriodoManutencao(inicio, inicio.plus(5, ChronoUnit.DAYS)), UUID.randomUUID(), inicio);
    }

    @Test
    void concluiEVoltaParaDisponivel() {
        when(manutencaoRepository.findEmAbertoPorAtivo(any())).thenReturn(Optional.of(emAberto()));
        var response = useCase.execute(ativoId);
        assertThat(response.status()).isEqualTo(AtivoStatus.DISPONIVEL);
        verify(manutencaoRepository).save(any(AtivoManutencao.class));
    }

    @Test
    void falhaSemManutencaoEmAberto() {
        when(manutencaoRepository.findEmAbertoPorAtivo(any())).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute(ativoId)).isInstanceOf(AtivoManutencaoInvalidaException.class);
    }
}
