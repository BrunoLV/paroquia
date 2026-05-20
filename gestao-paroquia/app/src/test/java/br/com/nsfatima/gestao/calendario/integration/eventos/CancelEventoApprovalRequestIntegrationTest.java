package br.com.nsfatima.gestao.calendario.integration.eventos;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
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
class CancelEventoApprovalRequestIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void shouldCreatePendingApprovalForPastoralCoordinator() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity entity = new EventoEntity();
        entity.setId(eventoId);
        entity.setTitulo("Solicitar cancelamento");
        entity.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        entity.setInicioUtc(Instant.parse("2026-09-10T10:00:00Z"));
        entity.setFimUtc(Instant.parse("2026-09-10T11:00:00Z"));
        entity.setStatus("CONFIRMADO");
        eventoJpaRepository.save(entity);

        mockMvc.perform(delete("/api/v1/eventos/{eventoId}", eventoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", "00000000-0000-0000-0000-0000000000aa")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          \"motivo\": \"Aguardando aprovacao do conselho\"
                        }
                        """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.eventoId").value(eventoId.toString()))
                .andExpect(jsonPath("$.mensagem").value("APPROVAL_PENDING"));

        EventoEntity savedEvento = Objects.requireNonNull(eventoJpaRepository.findById(eventoId).orElse(null));
        assertThat(savedEvento.getStatus()).isEqualTo("CONFIRMADO");

        AprovacaoEntity aprovacao = aprovacaoJpaRepository
                .findTopByEventoIdAndTipoSolicitacaoAndStatusIgnoreCaseOrderByCriadoEmUtcDesc(eventoId, "CANCELAMENTO",
                        "PENDENTE")
                .orElseThrow();
        assertThat(aprovacao.getMotivoCancelamentoSnapshot()).isEqualTo("Aguardando aprovacao do conselho");
        assertThat(aprovacao.getSolicitantePapel()).isEqualTo("coordenador");
        assertThat(aprovacao.getSolicitanteTipoOrganizacao()).isEqualTo("PASTORAL");
    }
}
