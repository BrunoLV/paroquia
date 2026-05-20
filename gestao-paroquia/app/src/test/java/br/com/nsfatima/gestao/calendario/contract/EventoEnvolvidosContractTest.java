package br.com.nsfatima.gestao.calendario.contract;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = "classpath:sql/security-fixtures.sql")
class EventoEnvolvidosContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoRepository;

    private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        EventoEntity entity = new EventoEntity();
        entity.setId(EVENT_ID);
        entity.setTitulo("Evento Teste");
        entity.setOrganizacaoResponsavelId(ORG_ID);
        entity.setInicioUtc(Instant.now());
        entity.setFimUtc(Instant.now().plusSeconds(3600));
        entity.setStatus("CONFIRMADO");
        eventoRepository.save(entity);
    }

    @Test
    void shouldUpdateEnvolvidos() throws Exception {
        mockMvc.perform(put("/api/v1/eventos/{eventoId}/envolvidos", EVENT_ID)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("{\"envolvidos\":[{\"organizacaoId\":\"00000000-0000-0000-0000-0000000000aa\", \"papel\": \"RESPONSAVEL\"}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventoId").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.envolvidos[0].organizacaoId").value("00000000-0000-0000-0000-0000000000aa"))
                .andExpect(jsonPath("$.envolvidos[0].papel").value("RESPONSAVEL"));
    }
}
