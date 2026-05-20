package br.com.nsfatima.gestao.calendario.contract;

import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AprovacoesContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    private static final UUID ORG_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID EVENT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @BeforeEach
    void setUp() {
        aprovacaoJpaRepository.deleteAll();
        eventoJpaRepository.deleteAll();

        EventoEntity evento = new EventoEntity();
        evento.setId(EVENT_ID);
        evento.setTitulo("Evento Existente");
        evento.setOrganizacaoResponsavelId(ORG_ID);
        evento.setInicioUtc(Instant.now().plusSeconds(3600));
        evento.setFimUtc(Instant.now().plusSeconds(7200));
        evento.setStatus("CONFIRMADO");
        eventoJpaRepository.save(evento);
    }

    @Test
    void shouldCreateAprovacaoWithExplicitDto() throws Exception {
        mockMvc.perform(post("/api/v1/aprovacoes")
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "PASTORAL")
                .header("X-Actor-Org-Id", ORG_ID.toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content(
                        "{\"eventoId\":\"%s\",\"tipoSolicitacao\":\"alteracao_horario\"}".formatted(EVENT_ID)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.eventoId").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.tipoSolicitacao").value("ALTERACAO_HORARIO"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    @SuppressWarnings("null")
    void shouldPatchApprovalDecisionWithExecutionOutcome() throws Exception {
        UUID aprovacaoId = UUID.randomUUID();
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setEventoId(EVENT_ID);
        aprovacao.setTipoSolicitacao("CANCELAMENTO");
        aprovacao.setAprovadorPapel("conselho-coordenador");
        aprovacao.setStatus("PENDENTE");
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setMotivoCancelamentoSnapshot("Aprovado no conselho.");
        aprovacaoJpaRepository.save(aprovacao);

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                .header("X-Actor-Role", "coordenador")
                .header("X-Actor-Org-Type", "CONSELHO")
                .header("X-Actor-Org-Id", ORG_ID.toString())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .content("""
                        {
                            \"status\": \"APROVADA\",
                            \"observacao\": \"Executar cancelamento\"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(aprovacaoId.toString()))
                .andExpect(jsonPath("$.status").value("APROVADA"))
                .andExpect(jsonPath("$.actionExecution.outcome").value("EXECUTED"))
                .andExpect(jsonPath("$.actionExecution.eventoId").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.actionExecution.eventStatus").value("CANCELADO"));
    }
}
