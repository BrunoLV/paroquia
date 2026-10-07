package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.domain.policy.PadraoEventoPatchAuthorizationPolicy;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.iam.application.service.PapelGlobalAuthorizationService;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno.Vinculo;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EventoPatchAuthorizationServiceTest {

    private EventoPatchAuthorizationService service;
    private SecurityContext originalContext;

    @BeforeEach
    void setUp() {
        service = new EventoPatchAuthorizationService(
                mock(PapelGlobalAuthorizationService.class), new PadraoEventoPatchAuthorizationPolicy());
        originalContext = SecurityContextHolder.getContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.setContext(originalContext);
    }

    private void mockAuthentication(UUID usuarioId, List<Vinculo> vinculos) {
        var principal = new UsuarioDetails(usuarioId, "tester", "pass", true, "", vinculos);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        var context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);
    }

    @Test
    void devePermitirEventoCriacaoQuandoUsuarioECoordenadorDeMesmaPastoralEMesmaIgreja() {
        UUID usuarioId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID churchId = UUID.randomUUID();

        var vinculo = new Vinculo(orgId,
                "Pastoral familiar", TipoOrganizacao.PASTORAL.name(), null, null, null, true,
                PapelOrganizacional.COORDENADOR.name(), churchId);

        mockAuthentication(usuarioId, List.of(vinculo));

        var actorContext = new EventoActorContext("tester",
                PapelOrganizacional.COORDENADOR.storedValue(), TipoOrganizacao.PASTORAL.name(), orgId,
                        usuarioId,
                churchId);


        assertDoesNotThrow(() -> service.assertCanCreate(actorContext, orgId, churchId));
    }

    @Test
    void deveNegarEventoCriacaoQuandoUsuarioECoordenadorDeMesmaPastoralButDiferenteIgreja() {
        UUID usuarioId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID churchA = UUID.randomUUID();
        UUID churchB = UUID.randomUUID();

        var vinculo = new Vinculo(orgId,
                "Pastoral familiar", TipoOrganizacao.PASTORAL.name(), null, null, null, true,
                PapelOrganizacional.COORDENADOR.name(), churchA);

        mockAuthentication(usuarioId, List.of(vinculo));

        var actorContext = new EventoActorContext("tester",
                PapelOrganizacional.COORDENADOR.storedValue(), TipoOrganizacao.PASTORAL.name(), orgId,
                        usuarioId,
                churchA);


        assertThrows(AccessDeniedException.class, () -> service.assertCanCreate(actorContext, orgId, churchB));
    }

    @Test
    void devePermitirEventoCriacaoQuandoUsuarioEParoco() {
        UUID usuarioId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();
        UUID churchId = UUID.randomUUID();

        var vinculo = new Vinculo(orgId,
                "Clero", "CLERO", null, null, null, true, PapelGlobal.PAROCO.name(),
                null);

        mockAuthentication(usuarioId, List.of(vinculo));

        var actorContext = new EventoActorContext("tester",
                PapelGlobal.PAROCO.storedValue(), "CLERO", orgId, usuarioId, null);


        assertDoesNotThrow(() -> service.assertCanCreate(actorContext, orgId, churchId));
    }

    @Test
    void devePermitirDecisaoPastoralImediataQuandoUsuarioEParoco() {
        UUID usuarioId = UUID.randomUUID();
        var actorContext = new EventoActorContext("tester",
                PapelGlobal.PAROCO.storedValue(), "CLERO", UUID.randomUUID(), usuarioId,
                        null);

        assertDoesNotThrow(() -> service.assertCanLiberarConflitoAgenda(actorContext));
    }

    @Test
    void deveNegarDecisaoPastoralImediataQuandoUsuarioENaoParoco() {
        UUID usuarioId = UUID.randomUUID();
        var actorContext = new EventoActorContext("tester",
                PapelOrganizacional.COORDENADOR.storedValue(), TipoOrganizacao.PASTORAL.name(),
                        UUID.randomUUID(), usuarioId, null);

        assertThrows(AccessDeniedException.class, () -> service.assertCanLiberarConflitoAgenda(actorContext));
    }

    @Test
    void devePermitirEventoCriacaoQuandoUsuarioECoordenadorDePastoralSemIgrejaVincular() {
        UUID usuarioId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        var vinculo = new Vinculo(orgId,
                "Pastoral familiar", TipoOrganizacao.PASTORAL.name(), null, null, null, true,
                PapelOrganizacional.COORDENADOR.name(), null);

        mockAuthentication(usuarioId, List.of(vinculo));

        var actorContext = new EventoActorContext("tester",
                PapelOrganizacional.COORDENADOR.storedValue(), TipoOrganizacao.PASTORAL.name(), orgId,
                        usuarioId,
                null);


        assertDoesNotThrow(() -> service.assertCanCreate(actorContext, orgId, null));
    }
}
