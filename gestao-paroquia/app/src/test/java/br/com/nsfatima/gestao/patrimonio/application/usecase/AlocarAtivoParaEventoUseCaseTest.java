package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AlocarAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.application.usecase.observacao.CreateNotaAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoCategoria;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoVinculacaoPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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
    private EventoActorContextResolver actorContextResolver;

    @Mock
    private CreateNotaAtivoPatrimonialUseCase createNotaUseCase;

    @Mock
    private PatrimonioAuditPublisher auditPublisher;

    private AlocarAtivoParaEventoUseCase useCase;
    private UUID ativoId;
    private UUID eventoId;
    private UUID localId;
    private UUID organizacaoId;

    @BeforeEach
    void setUp() {
        useCase = new AlocarAtivoParaEventoUseCase(ativoRepository, eventoRepository, localRepository,
                actorContextResolver, new PatrimonioAuthorizationService(), new AtivoVinculacaoPolicy(),
                createNotaUseCase, auditPublisher);
        ativoId = UUID.randomUUID();
        eventoId = UUID.randomUUID();
        localId = UUID.randomUUID();
        organizacaoId = UUID.randomUUID();

        lenient().when(actorContextResolver.resolveRequired())
                .thenReturn(new EventoActorContext("admin", "admin", "CLERO", null, UUID.randomUUID(), null));
    }

    @Test
    void deveAlocarAtivoDisponivelParaEventoDaMesmaOrganizacao() {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, organizacaoId);
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(criarEvento()));
        when(localRepository.findById(localId)).thenReturn(Optional.of(criarLocal(organizacaoId)));
        when(ativoRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.execute(ativoId,
                new AlocarAtivoRequest(eventoId, "Utilização para exibição de slides"));

        assertThat(response.status()).isEqualTo(AtivoStatus.ALOCADO);
        verify(createNotaUseCase).execute(eq(ativoId), any(), any(), any());
        verify(auditPublisher).publishAlocacao(any(), any(), any(), any());
    }

    @Test
    void deveRejeitarAlocacaoQuandoOrganizacaoDoLocalDiverge() {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.DISPONIVEL, organizacaoId);
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(criarEvento()));
        when(localRepository.findById(localId)).thenReturn(Optional.of(criarLocal(UUID.randomUUID())));

        assertThatThrownBy(() -> useCase.execute(ativoId,
                new AlocarAtivoRequest(eventoId, "Utilização para exibição de slides")))
                .isInstanceOf(AtivoAlocacaoInvalidaException.class);
    }

    @Test
    void deveRejeitarAlocacaoDeAtivoJaAlocado() {
        AtivoPatrimonialEntity ativo = criarAtivo(AtivoStatus.ALOCADO, organizacaoId);
        when(ativoRepository.findById(ativoId)).thenReturn(Optional.of(ativo));
        when(eventoRepository.findById(eventoId)).thenReturn(Optional.of(criarEvento()));
        when(localRepository.findById(localId)).thenReturn(Optional.of(criarLocal(organizacaoId)));

        assertThatThrownBy(() -> useCase.execute(ativoId,
                new AlocarAtivoRequest(eventoId, "Utilização para exibição de slides")))
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
