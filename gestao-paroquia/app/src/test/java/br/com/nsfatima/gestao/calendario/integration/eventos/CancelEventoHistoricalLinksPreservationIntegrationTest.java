package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoEnvolvidoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
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
class CancelEventoHistoricalLinksPreservationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @Autowired
    private EventoEnvolvidoJpaRepository eventoEnvolvidoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void shouldPreserveHistoricalLinksAfterCancellation() throws Exception {
        UUID eventoId = UUID.randomUUID();
        UUID organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");

        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Preservar vinculos");
        evento.setOrganizacaoResponsavelId(organizacaoId);
        evento.setInicioUtc(Instant.parse("2026-12-14T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-12-14T11:00:00Z"));
        evento.setStatus("CONFIRMADO");
        eventoJpaRepository.save(evento);

        ObservacaoEventoEntity preexistente = new ObservacaoEventoEntity();
        preexistente.setId(UUID.randomUUID());
        preexistente.setEventoId(eventoId);
        preexistente.setUsuarioId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
        preexistente.setTipo("GERAL");
        preexistente.setConteudo("Observacao preexistente");
        preexistente.setCriadoEmUtc(Instant.now());
        observacaoEventoJpaRepository.save(preexistente);

        EventoEnvolvidoEntity envolvido = new EventoEnvolvidoEntity();
        envolvido.setEventoId(eventoId);
        envolvido.setOrganizacaoId(UUID.fromString("00000000-0000-0000-0000-0000000000bb"));
        envolvido.setPapelParticipacao(PapelEnvolvido.APOIO);
        eventoEnvolvidoJpaRepository.save(envolvido);

        mockMvc.perform(delete("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"motivo\": \"Preservar historico\"
                        }
                        """))
                .andExpect(status().isOk());

        EventoEntity atualizado = eventoJpaRepository.findById(eventoId).orElseThrow();
        assertThat(atualizado.getStatus()).isEqualTo("CANCELADO");
        assertThat(atualizado.getOrganizacaoResponsavelId()).isEqualTo(organizacaoId);
        assertThat(eventoEnvolvidoJpaRepository.findByEventoId(eventoId)).hasSize(1);
        assertThat(observacaoEventoJpaRepository.findByEventoId(eventoId))
                .extracting(ObservacaoEventoEntity::getConteudo)
                .contains("Observacao preexistente", "Preservar historico");
    }
}
