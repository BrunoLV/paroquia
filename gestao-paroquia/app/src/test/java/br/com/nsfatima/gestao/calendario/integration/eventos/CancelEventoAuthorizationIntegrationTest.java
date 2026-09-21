package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class CancelEventoAuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    static Stream<Arguments> forbiddenActors() {
        return Stream.of(
                Arguments.of("voluntario-pastoral-local", "PASTORAL"),
                Arguments.of("membro-pastoral", "PASTORAL"),
                Arguments.of("padre", "CLERO"));
    }

    @ParameterizedTest
    @MethodSource("forbiddenActors")
    @SuppressWarnings("null")
    void deveRejeitarNaoAutorizadoPapeis(String role, String orgType) throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Evento protegido");
        evento.setGrupoParoquialResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        evento.setInicioUtc(Instant.parse("2026-10-10T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-10T11:00:00Z"));
        evento.setStatus("CONFIRMADO");
        eventoJpaRepository.save(evento);

        mockMvc.perform(post("/api/v1/eventos/{eventoId}/cancelar", eventoId)
                        .header("X-Actor-Role", role)
                        .header("X-Actor-Org-Type", orgType)
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "motivo": "Sem autorizacao"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));

        assertThat(eventoJpaRepository.findById(eventoId).orElseThrow().getStatus()).isEqualTo("CONFIRMADO");
    }

    @Test
    void secretarioDeveGerarPendenteAoCancelarComJustificativa() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Evento a ser cancelado pelo secretario");
        evento.setGrupoParoquialResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        evento.setInicioUtc(Instant.parse("2026-10-10T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-10T11:00:00Z"));
        evento.setStatus("CONFIRMADO");
        eventoJpaRepository.save(evento);

        mockMvc.perform(post("/api/v1/eventos/{eventoId}/cancelar", eventoId)
                        .header("X-Actor-Role", "secretario")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "motivo": "Justificativa formal do secretario para cancelamento"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"));

        assertThat(eventoJpaRepository.findById(eventoId).orElseThrow().getStatus()).isEqualTo("CONFIRMADO");
    }

    @Test
    void secretarioSemJustificativaAoCancelarDeveRetornar400() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Evento sem justificativa");
        evento.setGrupoParoquialResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        evento.setInicioUtc(Instant.parse("2026-10-10T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-10T11:00:00Z"));
        evento.setStatus("CONFIRMADO");
        eventoJpaRepository.save(evento);

        mockMvc.perform(post("/api/v1/eventos/{eventoId}/cancelar", eventoId)
                        .header("X-Actor-Role", "secretario")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "motivo": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}
