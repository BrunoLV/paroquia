package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CadastrarAtivoPatrimonialRequest;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoGrupoParoquialPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.NumeroTombamentoGenerator;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarAtivoPatrimonialUseCaseTest {

    @Mock private AtivoPatrimonialRepository repository;
    @Mock private EventoActorContextResolver actorContextResolver;
    @Mock private PatrimonioAuthorizationService authorizationService;
    @Mock private AtivoGrupoParoquialPolicy grupoParoquialPolicy;
    @Mock private NumeroTombamentoGenerator numeroTombamentoGenerator;
    @Mock private PatrimonioAuditPublisher auditPublisher;

    @Test
    void cadastraComTombamentoGeradoEStatusDisponivel() {
        var useCase = new CadastrarAtivoPatrimonialUseCase(repository, actorContextResolver, authorizationService,
                grupoParoquialPolicy, numeroTombamentoGenerator, auditPublisher);
        UUID grupo = UUID.randomUUID();
        UUID autor = UUID.randomUUID();
        when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("a", "admin", "CLERO", null, autor, null));
        when(numeroTombamentoGenerator.generate(anyInt())).thenReturn("PAT-2026-0009");
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var request = new CadastrarAtivoPatrimonialRequest("Caixa de Som", "12 polegadas",
                AtivoCategoria.ELETRONICO, grupo, true, LocalDate.of(2025, 1, 1), BigDecimal.valueOf(1200));

        var response = useCase.execute(request);

        assertThat(response.status()).isEqualTo(AtivoStatus.DISPONIVEL);
        assertThat(response.codigoTombamento()).isEqualTo("PAT-2026-0009");
        assertThat(response.cadastradoPorId()).isEqualTo(autor);
        verify(grupoParoquialPolicy).assertGrupoParoquialProprietariaPermitida(grupo);
        ArgumentCaptor<AtivoPatrimonial> captor = ArgumentCaptor.forClass(AtivoPatrimonial.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().isReservavel()).isTrue();
    }
}
