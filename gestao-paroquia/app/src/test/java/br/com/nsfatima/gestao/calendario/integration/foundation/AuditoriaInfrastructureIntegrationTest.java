package br.com.nsfatima.gestao.calendario.integration.foundation;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.observabilidade.application.service.AuditoriaPersistenceService;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
@SpringBootTest
class AuditoriaInfrastructureIntegrationTest {

    @Autowired
    private AuditoriaPersistenceService auditLogPersistenceService;

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
    void devePersistirEstruturadoAuditarTrilhaParaEventoMutacao() {
        UUID grupoParoquialId = UUID.fromString("00000000-0000-0000-0000-0000000000aa");
        UUID eventoId = UUID.randomUUID();

        EventoEntity evento = new EventoEntity();
        evento.setId(eventoId);
        evento.setTitulo("Infra auditoria");
        evento.setGrupoParoquialResponsavelId(grupoParoquialId);
        evento.setInicioUtc(Instant.parse("2026-10-01T10:00:00Z"));
        evento.setFimUtc(Instant.parse("2026-10-01T11:00:00Z"));
        evento.setStatus("RASCUNHO");
        eventoJpaRepository.save(evento);

        auditLogPersistenceService.log("tester", "atualizar", eventoId.toString(), "sucesso", Map.of(
                "grupoParoquialId", grupoParoquialId,
                "contextId", eventoId,
                "agendaAlterada", true,
                "correlationId", "corr-infra-001"));

        assertThat(auditoriaOperacaoJpaRepository.findAll())
                .hasSize(1)
                .first()
                .satisfies(record -> {
                    assertThat(record.getGrupoParoquialId()).isEqualTo(grupoParoquialId);
                    assertThat(record.getEventoId()).isEqualTo(eventoId);

                    assertThat(record.getAcao()).isEqualTo("atualizar");
                    assertThat(record.getCorrelationId()).isEqualTo("corr-infra-001");
                    assertThat(record.getDetalhesAuditaveisJson()).contains("agendaAlterada");
                });
    }
}
