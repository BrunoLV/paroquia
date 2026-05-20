package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CancelEventoImmediateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void shouldCancelConfirmedEventoImmediatelyForParoco() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("Cancelamento imediato");
        entity.setDescricao("descricao base");
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        entity.setInicioUtc(Instant.parse("2026-10-01T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-10-01T11:00:00Z"));
        entity.setStatus("CONFIRMADO");
        eventoJpaRepository.save(entity);

        mockMvc.perform(delete("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Role", "paroco")
                .header("X-Actor-Org-Type", "CLERO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"motivo\": \"Evento suprimido pelo conselho paroquial\"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"))
                .andExpect(jsonPath("$.canceladoMotivo").value("Evento suprimido pelo conselho paroquial"));

        EventoEntity saved = Objects.requireNonNull(eventoJpaRepository.findById(eventoId).orElse(null));
        assertThat(saved.getStatus()).isEqualTo("CANCELADO");
        assertThat(saved.getCanceladoMotivo()).isEqualTo("Evento suprimido pelo conselho paroquial");

        List<ObservacaoEventoEntity> observacoes = observacaoEventoJpaRepository.findByEventoId(eventoId);
        assertThat(observacoes).hasSize(1);
        assertThat(observacoes.get(0).getTipo()).isEqualTo("CANCELAMENTO");
        assertThat(observacoes.get(0).getConteudo()).isEqualTo("Evento suprimido pelo conselho paroquial");
    }
}
