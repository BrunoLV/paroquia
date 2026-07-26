package br.com.nsfatima.gestao.calendario.integration.auditoria;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditPersistenceFailureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @MockitoBean
    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;


    @AfterEach
    void tearDown() {
        reset(auditoriaOperacaoJpaRepository);
    }

    @Test
    @SuppressWarnings("null")
    void shouldReturnConflictWhenMandatoryAuditPersistenceFails() throws Exception {
        UUID organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Falha auditavel");
        evento.setDescricao("descricao-original");
        evento.setOrganizacaoResponsavelId(organizacaoId);
        evento.setInicioUtc(Instant.parse("2026-10-01T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-01T11:00:00Z"));
        evento.setStatus("RASCUNHO");
        eventoJpaRepository.save(evento);

        doThrow(new RuntimeException("db down"))
                .when(auditoriaOperacaoJpaRepository)
                .save(any(AuditoriaOperacaoEntity.class));

        mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
                        .header("X-Actor-Org-Id", organizacaoId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{" +
                                "\"descricao\":\"descricao-atualizada\"" +
                                "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("AUDIT_PERSISTENCE_REQUIRED"));

        assertThat(eventoJpaRepository.findById(eventoId))
                .isPresent()
                .get()
                .extracting(EventoEntity::getDescricao)
                .isEqualTo("descricao-original");
    }
}
