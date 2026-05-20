package br.com.nsfatima.gestao.calendario.integration.foundation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AuditoriaInfrastructureIntegrationTest {

    @Autowired
    private AuditLogPersistenceService auditLogPersistenceService;

    @Autowired
    private AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;

    @Autowired
    private EventoJpaRepository eventoJpaRepository;

    @BeforeEach
    void setUp() {
        auditoriaOperacaoJpaRepository.deleteAll();
        eventoJpaRepository.deleteAll();
    }

    @Test
    @SuppressWarnings("null")
    void shouldPersistStructuredAuditTrailForEventMutation() {
        UUID organizacaoId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        UUID eventoId = UUID.randomUUID();

        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Infra auditoria");
        evento.setOrganizacaoResponsavelId(organizacaoId);
        evento.setInicioUtc(Instant.parse("2026-10-01T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-01T11:00:00Z"));
        evento.setStatus("RASCUNHO");
        eventoJpaRepository.save(evento);

        auditLogPersistenceService.log("tester", "patch", eventoId.toString(), "success", Map.of(
                "organizacaoId", organizacaoId,
                "contextId", eventoId,
                "scheduleChanged", true,
                "correlationId", "corr-infra-001"));

        assertThat(auditoriaOperacaoJpaRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(record -> {
                    assertThat(record.getOrganizacaoId()).isEqualTo(organizacaoId);
                    assertThat(record.getEventoId()).isEqualTo(eventoId);

                    assertThat(record.getAcao()).isEqualTo("patch");
                    assertThat(record.getCorrelationId()).isEqualTo("corr-infra-001");
                    assertThat(record.getDetalhesAuditaveisJson()).contains("scheduleChanged");
                });
    }
}
