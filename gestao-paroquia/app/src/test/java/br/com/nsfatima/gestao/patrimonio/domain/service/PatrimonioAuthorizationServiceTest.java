package br.com.nsfatima.gestao.patrimonio.domain.service;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PatrimonioAuthorizationServiceTest {

    private final PatrimonioAuthorizationService service = new PatrimonioAuthorizationService();
    private final UUID organizacaoId = UUID.randomUUID();

    @Test
    void deveRejeitarPapelNaoAutorizado() {
        EventoActorContext actor = new EventoActorContext("joao", "membro-pastoral", "PASTORAL",
                organizacaoId, UUID.randomUUID(), null);

        assertThatThrownBy(() -> service.assertCanCreate(actor, organizacaoId))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void deveRejeitarCoordenadorParaOrganizacaoDiferente() {
        EventoActorContext actor = new EventoActorContext("joao", "coord-pastoral-local", "PASTORAL",
                organizacaoId, UUID.randomUUID(), null);
        UUID outraOrganizacaoId = UUID.randomUUID();

        assertThatThrownBy(() -> service.assertCanCreate(actor, outraOrganizacaoId))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void devePermitirCoordenadorParaSuaPropriaOrganizacao() {
        EventoActorContext actor = new EventoActorContext("joao", "coord-pastoral-local", "PASTORAL",
                organizacaoId, UUID.randomUUID(), null);

        assertThatCode(() -> service.assertCanCreate(actor, organizacaoId)).doesNotThrowAnyException();
    }

    @Test
    void devePermitirAdminParaQualquerOrganizacao() {
        EventoActorContext actor = new EventoActorContext("admin", "admin", "CLERO",
                null, UUID.randomUUID(), null);

        assertThatCode(() -> service.assertCanCreate(actor, UUID.randomUUID())).doesNotThrowAnyException();
    }

    @Test
    void devePermitirParocoParaQualquerOrganizacao() {
        EventoActorContext actor = new EventoActorContext("paroco", "paroco", "CLERO",
                UUID.randomUUID(), UUID.randomUUID(), null);

        assertThatCode(() -> service.assertCanTransfer(actor, UUID.randomUUID())).doesNotThrowAnyException();
    }
}
