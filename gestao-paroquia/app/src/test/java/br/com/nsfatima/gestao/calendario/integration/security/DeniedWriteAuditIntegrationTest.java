package br.com.nsfatima.gestao.calendario.integration.security;

import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class DeniedWriteAuditIntegrationTest {

    @Test
    void deveRegistrarAuditarEmNegadoEscrever() {
        AuditLogPersistenceService service = new AuditLogPersistenceService(
                mock(AuditoriaOperacaoJpaRepository.class),
                new ObjectMapper());
        assertDoesNotThrow(() -> service.log("user", "write-denied", "evento", "ACCESS_DENIED", Map.of()));

        EventoAuditPublisher publisher = new EventoAuditPublisher(service);
        assertDoesNotThrow(() -> publisher.publishDeniedWrite("user", "evento"));
    }
}

