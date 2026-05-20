package br.com.nsfatima.gestao.calendario.integration.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.ApprovalActionPayloadMapper;
import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ObservacaoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

class DeniedWriteAuditIntegrationTest {

    @Test
    void shouldLogAuditOnDeniedWrite() {
        AuditLogPersistenceService service = new AuditLogPersistenceService(
                mock(AuditoriaOperacaoJpaRepository.class),
                new ObjectMapper());
        assertDoesNotThrow(() -> service.log("user", "write-denied", "evento", "ACCESS_DENIED", Map.of()));

        EventoAuditPublisher publisher = new EventoAuditPublisher(service);
        assertDoesNotThrow(() -> publisher.publishDeniedWrite("user", "evento"));
    }
}

