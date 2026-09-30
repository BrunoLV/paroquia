package br.com.nsfatima.gestao.patrimonio.application.service;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
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
        EventoActorContext actor = new EventoActorContext("joao", "membro-ativo", "PASTORAL",
                grupoParoquialId, UUID.randomUUID(), null);

        assertThatThrownBy(() -> service.assertPodeGerirPatrimonio(actor, grupoParoquialId))
                .isInstanceOf(OperacaoProibidaException.class);
    }

    @Test
    void deveRejeitarCoordenadorParaGrupoParoquialDiferente() {
        EventoActorContext actor = new EventoActorContext("joao", "coordenador", "PASTORAL",
                grupoParoquialId, UUID.randomUUID(), null);
        UUID outraGrupoParoquialId = UUID.randomUUID();

        assertThatThrownBy(() -> service.assertPodeGerirPatrimonio(actor, outraGrupoParoquialId))
                .isInstanceOf(OperacaoProibidaException.class);
    }

    @Test
    void devePermitirCoordenadorParaSuaPropriaGrupoParoquial() {
        EventoActorContext actor = new EventoActorContext("joao", "coordenador", "PASTORAL",
                grupoParoquialId, UUID.randomUUID(), null);

        assertThatCode(() -> service.assertPodeGerirPatrimonio(actor, grupoParoquialId)).doesNotThrowAnyException();
    }

    @Test
    void deveRejeitarAdminSemPoderPatrimonialCanonicamente() {
        EventoActorContext actor = new EventoActorContext("admin", "admin", "GLOBAL",
                null, UUID.randomUUID(), null);

        assertThatThrownBy(() -> service.assertPodeGerirPatrimonio(actor, UUID.randomUUID()))
                .isInstanceOf(OperacaoProibidaException.class);
    }

    @Test
    void devePermitirParocoParaQualquerGrupoParoquial() {
        EventoActorContext actor = new EventoActorContext("paroco", "paroco", "CLERO",
                UUID.randomUUID(), UUID.randomUUID(), null);

        assertThatCode(() -> service.assertPodeGerirPatrimonio(actor, UUID.randomUUID())).doesNotThrowAnyException();
    }
}
