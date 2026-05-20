package br.com.nsfatima.gestao.calendario.integration.observability;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.CadastroEventoMetricsPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.Test;

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
