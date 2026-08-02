package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest
@AutoConfigureMockMvc
class CancelledEventoVisibilityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void deveOcultarDePublicoEShowEmAutorizadoInternoHistorico() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Cancelado visivel internamente");
        evento.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        evento.setInicioUtc(Instant.parse("2026-12-13T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-12-13T11:00:00Z"));
        evento.setStatus("CANCELADO");
        evento.setCanceladoMotivo("Auditoria interna");
        eventoJpaRepository.save(evento);

        mockMvc.perform(get("/api/v1/eventos").header("X-Test-Anonymous", "true"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/eventos?size=100")
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id").value(hasItem(eventoId.toString())));

    }
}
