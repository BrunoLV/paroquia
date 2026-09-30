package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class CancelEventoOrganizationScopeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void deveRejeitarPastoralForaResponsavelGrupoParoquialEscopo() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Escopo organizacional");
        evento.setGrupoParoquialResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        evento.setInicioUtc(Instant.parse("2026-10-11T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-11T11:00:00Z"));
        evento.setStatus("CONFIRMADO");
        eventoJpaRepository.save(evento);

        mockMvc.perform(post("/api/v1/eventos/{eventoId}/cancelar", eventoId)
                        .header("X-Actor-Role", "coordenador")
                        .header("X-Actor-Org-Type", "PASTORAL")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000bb")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "motivo": "Fora do escopo"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

        assertThat(eventoJpaRepository.findById(eventoId).orElseThrow().getStatus()).isEqualTo("CONFIRMADO");
    }
}
