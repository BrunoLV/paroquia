package br.com.nsfatima.gestao.calendario.contract;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class LifecycleTransitionRegressionTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private EventoJpaRepository eventoJpaRepository;

  @Test
  @SuppressWarnings("null")
  void shouldReturnInvalidStatusTransitionForCancellationOutsideConfirmedState() throws Exception {
    UUID eventoId = UUID.randomUUID();
    EventoEntity evento = new EventoEntity();
    evento.setId(eventoId);
    evento.setTitulo("Lifecycle regression");
    evento.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
    evento.setInicioUtc(Instant.parse("2026-10-12T10:00:00Z"));
    evento.setFimUtc(Instant.parse("2026-10-12T11:00:00Z"));
    evento.setStatus("RASCUNHO");
    eventoJpaRepository.save(evento);

    mockMvc.perform(delete("/api/v1/eventos/{eventoId}", eventoId)
        .header("X-Actor-Role", "paroco")
        .header("X-Actor-Org-Type", "CLERO")
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              \"motivo\": \"Nao pode cancelar fora de confirmado\"
            }
            """))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.errorCode").value("INVALID_STATUS_TRANSITION"));
  }

  @Test
  @SuppressWarnings("null")
  void shouldKeepDomainRejectionForSensitiveCancellationWithoutApproval() throws Exception {
    UUID eventoId = UUID.randomUUID();
    EventoEntity entity = new EventoEntity();
    entity.setId(eventoId);
    entity.setTitulo("Lifecycle regression");
    entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
    entity.setInicioUtc(Instant.parse("2026-10-01T10:00:00Z"));
    entity.setFimUtc(Instant.parse("2026-10-01T11:00:00Z"));
    entity.setStatus("RASCUNHO");
    eventoJpaRepository.save(entity);

    mockMvc.perform(patch("/api/v1/eventos/{eventoId}", eventoId)
        .header("X-Actor-Role", "coordenador")
        .header("X-Actor-Org-Type", "PASTORAL")
        .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
        .contentType(MediaType.APPLICATION_JSON_VALUE)
        .content("""
            {
              \"status\": \"cancelado\"
            }
            """))
        .andExpect(status().isAccepted())
        .andExpect(jsonPath("$.status").value("PENDENTE"));
  }
}
