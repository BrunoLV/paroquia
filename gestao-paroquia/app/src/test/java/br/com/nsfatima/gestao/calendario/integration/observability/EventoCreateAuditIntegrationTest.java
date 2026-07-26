package br.com.nsfatima.gestao.calendario.integration.observability;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class EventoCreateAuditIntegrationTest {

    @Test
    void shouldPublishCreateAuditForSuccessAndFailure() {
        AuditLogPersistenceService auditLogPersistenceService = new AuditLogPersistenceService(
                mock(AuditoriaOperacaoJpaRepository.class),
                new ObjectMapper());
        EventoAuditPublisher auditPublisher = new EventoAuditPublisher(auditLogPersistenceService);
        CadastroEventoMetricsPublisher metricsPublisher = new CadastroEventoMetricsPublisher();

        assertDoesNotThrow(() -> auditPublisher.publishCreateSuccess("system", "evt-001", false, null));
        assertDoesNotThrow(() -> auditPublisher.publishCreateFailure("system", "VALIDATION", "invalid payload"));
        assertDoesNotThrow(() -> metricsPublisher.publishCreateSuccess(false, false));
        assertDoesNotThrow(() -> metricsPublisher.publishCreateFailure("VALIDATION"));
    }
}
