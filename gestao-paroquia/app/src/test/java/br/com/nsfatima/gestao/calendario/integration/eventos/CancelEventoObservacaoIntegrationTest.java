package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class CancelEventoObservacaoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @Autowired
    private AprovacaoJpaRepository aprovacaoJpaRepository;

    @Autowired
    private ObservacaoEventoJpaRepository observacaoEventoJpaRepository;

    @Test
    @SuppressWarnings("null")
    void deveAnexarCancelamentoObservacaoDepoisAprovadoExecucao() throws Exception {
        UUID eventoId = UUID.randomUUID();
        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Observacao apos aprovacao");
        evento.setOrganizacaoResponsavelId(UUID.fromString("00000000-0000-0000-0000-0000000000aa"));
        evento.setInicioUtc(Instant.parse("2026-12-11T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-12-11T11:00:00Z"));
        evento.setStatus("CONFIRMADO");
        eventoJpaRepository.save(evento);

        UUID aprovacaoId = UUID.randomUUID();
        AprovacaoEntity aprovacao = new AprovacaoEntity();
        aprovacao.setId(aprovacaoId);
        aprovacao.setEventoId(eventoId);
        aprovacao.setTipoSolicitacao("CANCELAMENTO");
        aprovacao.setAprovadorPapel("conselho-coordenador");
        aprovacao.setStatus(AprovacaoStatus.PENDENTE);
        aprovacao.setCriadoEmUtc(Instant.now());
        aprovacao.setMotivoCancelamentoSnapshot("Observacao de cancelamento aprovada");
        aprovacaoJpaRepository.save(aprovacao);

        mockMvc.perform(patch("/api/v1/aprovacoes/{id}", aprovacaoId)
                        .header("X-Actor-Role", "presidente-cpc")
                        .header("X-Actor-Org-Type", "CONSELHO")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "status": "APROVADA"
                                }
                                """))
                .andExpect(status().isOk());

        assertThat(observacaoEventoJpaRepository.findByEventoId(eventoId))
                .anyMatch(obs -> "CANCELAMENTO".equals(obs.getTipo())
                        && "Observacao de cancelamento aprovada".equals(obs.getConteudo()));
    }
}
