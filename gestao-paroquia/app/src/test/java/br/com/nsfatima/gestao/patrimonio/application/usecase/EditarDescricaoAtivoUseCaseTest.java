package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.EditarDescricaoAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNaoEncontradoException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.CodigoTombamento;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.patrimonio.application.service.PatrimonioAuthorizationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EditarDescricaoAtivoUseCaseTest {

    @Mock private AtivoPatrimonialRepository repository;
    @Mock private EventoActorContextResolver actorContextResolver;
    @Mock private PatrimonioAuthorizationService authorizationService;

    private final UUID ativoId = UUID.randomUUID();

    private EditarDescricaoAtivoUseCase useCase() {
        return new EditarDescricaoAtivoUseCase(repository, actorContextResolver, authorizationService);
    }

    private AtivoPatrimonial ativo(Instant deletedAt) {
        return new AtivoPatrimonial(new AtivoPatrimonialId(ativoId), "Projetor", CodigoTombamento.de(2026, 1),
                "antiga", AtivoCategoria.ELETRONICO, LocalDate.now(), BigDecimal.TEN, UUID.randomUUID(), true,
                AtivoStatus.DISPONIVEL, UUID.randomUUID(), Instant.now(), Instant.now(), deletedAt, 0L);
    }

    @Test
    void atualizaDescricao() {
        when(repository.findById(new AtivoPatrimonialId(ativoId))).thenReturn(Optional.of(ativo(null)));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        lenient().when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("a", "admin", "CLERO", null, UUID.randomUUID(), null));

        var response = useCase().execute(ativoId, new EditarDescricaoAtivoRequest("nova descrição"));

        assertThat(response.descricao()).isEqualTo("nova descrição");
    }

    @Test
    void naoEditaAtivoExcluido() {
        when(repository.findById(new AtivoPatrimonialId(ativoId))).thenReturn(Optional.of(ativo(Instant.now())));
        assertThatThrownBy(() -> useCase().execute(ativoId, new EditarDescricaoAtivoRequest("x")))
                .isInstanceOf(AtivoPatrimonialNaoEncontradoException.class);
    }
}
