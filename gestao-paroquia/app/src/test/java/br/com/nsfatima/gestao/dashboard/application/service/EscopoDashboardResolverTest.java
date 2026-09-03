package br.com.nsfatima.gestao.dashboard.application.service;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EscopoDashboardResolverTest {

    private final EscopoDashboardResolver resolver = new EscopoDashboardResolver();
    private final UUID orgAtor = UUID.randomUUID();
    private final UUID outraOrg = UUID.randomUUID();

    private EventoActorContext ator(UUID organizationId) {
        return new EventoActorContext("ator", "COORD", "PASTORAL", organizationId, UUID.randomUUID(), null);
    }

    @Test
    void atorGlobalSemFiltro_visaoGlobal() {
        assertThat(resolver.resolver(ator(null), null)).isEmpty();
    }

    @Test
    void atorGlobalComFiltro_usaOrganizacaoPedida() {
        assertThat(resolver.resolver(ator(null), outraOrg)).contains(outraOrg);
    }

    @Test
    void atorVinculadoSemFiltro_recaiNoProprioEscopo() {
        assertThat(resolver.resolver(ator(orgAtor), null)).contains(orgAtor);
    }

    @Test
    void atorVinculadoPedindoAPropriaOrg_permitido() {
        assertThat(resolver.resolver(ator(orgAtor), orgAtor)).contains(orgAtor);
    }

    @Test
    void atorVinculadoPedindoOutraOrg_negado() {
        assertThatThrownBy(() -> resolver.resolver(ator(orgAtor), outraOrg))
                .isInstanceOf(AccessDeniedException.class);
    }
}
