package br.com.nsfatima.gestao.calendario.contract;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import br.com.nsfatima.gestao.calendario.support.SecurityTestSupport;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class CalendarPayloadCompatibilityContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ORG_ID = UUID.fromString("00000000-0000-0000-0000-0000000000cc");

    @BeforeEach
    void setUp() {
        eventoJpaRepository.deleteAll();
        EventoEntity evento = new EventoEntity();
        evento.setId(EVENT_ID);
        evento.setTitulo("Evento Compatibilidade");
        evento.setOrganizacaoResponsavelId(ORG_ID);
        evento.setInicioUtc(Instant.now().plusSeconds(3600));
        evento.setFimUtc(Instant.now().plusSeconds(7200));
        evento.setStatus("CONFIRMADO");
        eventoJpaRepository.save(evento);
    }

    @Test
    void shouldPreserveApprovalPayloadShapeForAuthenticatedUsers() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "ana.conselho", "senha123");

        mockMvc.perform(post("/api/v1/aprovacoes")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"eventoId\": \"%s\",
                                  \"tipoSolicitacao\": \"alteracao_horario\"
                                }
                                """.formatted(EVENT_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventoId").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.tipoSolicitacao").value("ALTERACAO_HORARIO"))
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void shouldPreserveEnvolvidosCleanupPayloadForAuthenticatedUsers() throws Exception {
        MockHttpSession session = SecurityTestSupport.loginSession(mockMvc, "joao.silva", "senha123");

        mockMvc.perform(delete("/api/v1/eventos/{eventoId}/envolvidos", EVENT_ID)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventoId").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.envolvidos").isArray());
    }
}
