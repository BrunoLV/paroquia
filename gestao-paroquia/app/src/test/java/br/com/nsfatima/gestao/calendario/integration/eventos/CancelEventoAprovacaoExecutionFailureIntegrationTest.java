package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class CancelEventoAprovacaoExecutionFailureIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void deveFalhaSafelyQuandoAprovadoExecucaoPreconditionEBroken() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Falha segura");
        evento.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        evento.setInicioUtc(Instant.parse("2026-12-12T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-12-12T11:00:00Z"));
        evento.setStatus("RASCUNHO");
        eventoJpaRepository.save(evento);

        UUID aprovacaoId = UUID.randomUUID();
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setEventoId(eventoId);
        aprovacao.setTipoSolicitacao("CANCELAMENTO");
        aprovacao.setAprovadorPapel("conselho-coordenador");
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setMotivoCancelamentoSnapshot("Tentativa com evento nao confirmado");
        aprovacaoJpaRepository.save(aprovacao);

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APROVADA",
                                  "observacao": "Tentar executar"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("APROVACAO_EXECUCAO_FALHOU"));

        AprovacaoEntity saved = aprovacaoJpaRepository.findById(aprovacaoId).orElseThrow();
        assertThat(saved.getStatus()).isEqualTo(AprovacaoStatus.FALHA_EXECUCAO);
        assertThat(saved.getExecutadoEmUtc()).isNull();
        assertThat(eventoJpaRepository.findById(eventoId).orElseThrow().getStatus()).isEqualTo("RASCUNHO");
    }
}
