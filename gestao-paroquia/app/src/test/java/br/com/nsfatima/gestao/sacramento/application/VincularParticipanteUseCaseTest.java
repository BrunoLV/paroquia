package br.com.nsfatima.gestao.sacramento.application;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.AssentoResponse;
import br.com.nsfatima.gestao.sacramento.api.v1.dto.VincularParticipanteRequest;
import br.com.nsfatima.gestao.sacramento.application.port.out.ConsultaPessoaPort;
import br.com.nsfatima.gestao.sacramento.application.service.AssentoAcessoService;
import br.com.nsfatima.gestao.sacramento.application.service.SacramentoAuditoriaService;
import br.com.nsfatima.gestao.sacramento.application.usecase.VincularParticipanteUseCase;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramentalId;
import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.Participante;
import br.com.nsfatima.gestao.sacramento.domain.model.TeorAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.domain.policy.AlcadaSacramentalPolicy;
import br.com.nsfatima.gestao.sacramento.domain.repository.AssentoSacramentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class VincularParticipanteUseCaseTest {

    private final EventoActorContextResolver resolver = mock(EventoActorContextResolver.class);
    private final AssentoSacramentalRepository repository = mock(AssentoSacramentalRepository.class);
    private final ConsultaPessoaPort pessoas = mock(ConsultaPessoaPort.class);
    private final SacramentoAuditoriaService auditoria = mock(SacramentoAuditoriaService.class);
    private VincularParticipanteUseCase useCase;
    private AssentoSacramental assento;
    private Participante batizando;
    private final UUID fielId = UUID.randomUUID();

    @BeforeEach
    void preparar() {
        batizando = new Participante(UUID.randomUUID(), PapelParticipante.BATIZANDO, "Maria", null, null, null, null,
                null, null, null, null);
        assento = AssentoSacramental.lavrar(AssentoSacramentalId.gerar(), TipoSacramento.BATISMO, Visibilidade.COMUM,
                new TeorAssento(null, null, null, null, List.of(batizando), Map.of(), false), "x", Instant.now());
        when(repository.findById(any())).thenReturn(Optional.of(assento));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pessoas.nomeDoFiel(fielId)).thenReturn(Optional.of("Maria"));
        when(auditoria.capturar(any())).thenReturn("{}");
        AssentoAcessoService acesso = new AssentoAcessoService(repository, new AlcadaSacramentalPolicy());
        useCase = new VincularParticipanteUseCase(resolver, acesso, pessoas, repository, auditoria);
    }

    private void como(String papel) {
        when(resolver.resolveRequired()).thenReturn(new EventoActorContext("u", papel, "CLERO", null, null, null));
    }

    private boolean pendente(AssentoResponse r) {
        return r.participantes().getFirst().vinculoPendente();
    }

    @Test
    void vinculoDaSecretariaNasceUmPendente() {
        como("secretaria-paroquial");
        AssentoResponse r = useCase.vincular(assento.getId().value(), batizando.id(),
                new VincularParticipanteRequest(fielId, null));
        assertThat(pendente(r)).isTrue();
        assertThat(r.participantes().getFirst().fielId()).isEqualTo(fielId);
    }

    @Test
    void vinculoDoParocoNasceAprovado() {
        como("paroco");
        AssentoResponse r = useCase.vincular(assento.getId().value(), batizando.id(),
                new VincularParticipanteRequest(fielId, null));
        assertThat(pendente(r)).isFalse();
    }

    @Test
    void secretariaNaoAprovaNemRejeitaNemDesvincula() {
        como("paroco");
        useCase.vincular(assento.getId().value(), batizando.id(), new VincularParticipanteRequest(fielId, null));
        como("secretaria-paroquial");
        UUID id = assento.getId().value();
        assertThatThrownBy(() -> useCase.aprovar(id, batizando.id())).hasMessageContaining("APROVAR_VINCULO");
        assertThatThrownBy(() -> useCase.rejeitar(id, batizando.id())).hasMessageContaining("APROVAR_VINCULO");
        assertThatThrownBy(() -> useCase.desvincular(id, batizando.id())).hasMessageContaining("DESVINCULAR");
    }

    @Test
    void parocoAprovaVinculoPendenteDaSecretaria() {
        como("secretaria-paroquial");
        useCase.vincular(assento.getId().value(), batizando.id(), new VincularParticipanteRequest(fielId, null));
        como("paroco");
        AssentoResponse r = useCase.aprovar(assento.getId().value(), batizando.id());
        assertThat(pendente(r)).isFalse();
        assertThat(r.participantes().getFirst().fielId()).isEqualTo(fielId);
    }

    @Test
    void parocoRejeitaERemoveOPonteiro() {
        como("secretaria-paroquial");
        useCase.vincular(assento.getId().value(), batizando.id(), new VincularParticipanteRequest(fielId, null));
        como("paroco");
        AssentoResponse r = useCase.rejeitar(assento.getId().value(), batizando.id());
        assertThat(r.participantes().getFirst().fielId()).isNull();
        assertThat(r.participantes().getFirst().nomeHistorico()).isEqualTo("Maria");
    }
}
