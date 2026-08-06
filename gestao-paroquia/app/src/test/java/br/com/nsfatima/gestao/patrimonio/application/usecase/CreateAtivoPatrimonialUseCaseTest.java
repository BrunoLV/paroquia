package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CreateAtivoPatrimonialRequest;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoGrupoParoquialInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoGrupoParoquialPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.NumeroTombamentoGenerator;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAtivoPatrimonialUseCaseTest {

    @Mock
    private AtivoPatrimonialJpaRepository repository;

    @Mock
    private EventoActorContextResolver actorContextResolver;

    @Mock
    private NumeroTombamentoGenerator numeroTombamentoGenerator;

    @Mock
    private PatrimonioAuditPublisher auditPublisher;

    @Mock
    private AtivoGrupoParoquialPolicy grupoParoquialPolicy;

    private CreateAtivoPatrimonialUseCase useCase;
    private UUID grupoParoquialId;

    @BeforeEach
    void setUp() {
        useCase = new CreateAtivoPatrimonialUseCase(repository, actorContextResolver,
                new PatrimonioAuthorizationService(), grupoParoquialPolicy, numeroTombamentoGenerator, auditPublisher);
        grupoParoquialId = UUID.randomUUID();
        lenient().when(numeroTombamentoGenerator.generate(anyInt()))
                .thenReturn("PAT-" + Year.now().getValue() + "-0001");
        lenient().when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void deveCriarAtivoComStatusDisponivelParaCoordenadorDaPropriaGrupoParoquial() {
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("joao", "coord-pastoral-local", "PASTORAL",
                        grupoParoquialId, UUID.randomUUID(), null));

        var response = useCase.execute(new CreateAtivoPatrimonialRequest(
                "Projetor Epson", AtivoCategoria.ELETRONICO, grupoParoquialId, true,
                LocalDate.now(), BigDecimal.valueOf(2500)));

        assertThat(response.status()).isEqualTo(AtivoStatus.DISPONIVEL);
        assertThat(response.numeroTombamento()).startsWith("PAT-" + Year.now().getValue());
    }

    @Test
    void deveRejeitarCoordenadorCriandoParaOutraGrupoParoquial() {
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("joao", "coord-pastoral-local", "PASTORAL",
                        UUID.randomUUID(), UUID.randomUUID(), null));

        assertThatThrownBy(() -> useCase.execute(new CreateAtivoPatrimonialRequest(
                "Projetor Epson", AtivoCategoria.ELETRONICO, grupoParoquialId, true,
                LocalDate.now(), BigDecimal.valueOf(2500))))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void deveRejeitarCriacaoParaGrupoParoquialDeTipoInvalido() {
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("joao", "coord-pastoral-local", "PASTORAL",
                        grupoParoquialId, UUID.randomUUID(), null));
        doThrow(new AtivoGrupoParoquialInvalidaException("tipo inválido"))
                .when(grupoParoquialPolicy).assertGrupoParoquialProprietariaPermitida(grupoParoquialId);

        assertThatThrownBy(() -> useCase.execute(new CreateAtivoPatrimonialRequest(
                "Projetor Epson", AtivoCategoria.ELETRONICO, grupoParoquialId, true,
                LocalDate.now(), BigDecimal.valueOf(2500))))
                .isInstanceOf(AtivoGrupoParoquialInvalidaException.class);
    }
}
