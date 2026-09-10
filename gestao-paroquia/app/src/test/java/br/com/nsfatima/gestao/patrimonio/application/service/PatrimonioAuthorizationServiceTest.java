package br.com.nsfatima.gestao.patrimonio.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.patrimonio.domain.policy.PadraoAutorizacaoPatrimonioPolicy;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatrimonioAuthorizationServiceTest {

    private final PatrimonioAuthorizationService service =
            new PatrimonioAuthorizationService(new PadraoAutorizacaoPatrimonioPolicy());
    private final UUID grupoParoquialId = UUID.randomUUID();

    @Test
    void deveRejeitarPapelNaoAutorizado() {
        EventoActorContext actor = new EventoActorContext("joao", "membro-pastoral", "PASTORAL",
                grupoParoquialId, UUID.randomUUID(), null);

        assertThatThrownBy(() -> service.assertPodeGerirPatrimonio(actor, grupoParoquialId))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void deveRejeitarCoordenadorParaGrupoParoquialDiferente() {
        EventoActorContext actor = new EventoActorContext("joao", "coord-pastoral-local", "PASTORAL",
                grupoParoquialId, UUID.randomUUID(), null);
        UUID outraGrupoParoquialId = UUID.randomUUID();

        assertThatThrownBy(() -> service.assertPodeGerirPatrimonio(actor, outraGrupoParoquialId))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void devePermitirCoordenadorParaSuaPropriaGrupoParoquial() {
        EventoActorContext actor = new EventoActorContext("joao", "coord-pastoral-local", "PASTORAL",
                grupoParoquialId, UUID.randomUUID(), null);

        assertThatCode(() -> service.assertPodeGerirPatrimonio(actor, grupoParoquialId)).doesNotThrowAnyException();
    }

    @Test
    void devePermitirAdminParaQualquerGrupoParoquial() {
        EventoActorContext actor = new EventoActorContext("admin", "admin", "CLERO",
                null, UUID.randomUUID(), null);

        assertThatCode(() -> service.assertPodeGerirPatrimonio(actor, UUID.randomUUID())).doesNotThrowAnyException();
    }

    @Test
    void devePermitirParocoParaQualquerGrupoParoquial() {
        EventoActorContext actor = new EventoActorContext("paroco", "paroco", "CLERO",
                UUID.randomUUID(), UUID.randomUUID(), null);

        assertThatCode(() -> service.assertPodeGerirPatrimonio(actor, UUID.randomUUID())).doesNotThrowAnyException();
    }
}
