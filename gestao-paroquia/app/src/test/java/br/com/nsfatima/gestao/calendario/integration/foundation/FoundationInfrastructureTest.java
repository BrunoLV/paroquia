package br.com.nsfatima.gestao.calendario.integration.foundation;

import br.com.nsfatima.gestao.observabilidade.domain.service.AuditLogPersistenceService;
import br.com.nsfatima.gestao.calendario.infrastructure.time.TimezoneConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class FoundationInfrastructureTest {

    @Autowired
    private AuditLogPersistenceService auditLogPersistenceService;

    @Autowired
    private TimezoneConfig timezoneConfig;

    @Test
    void contextLoadsWithFoundationalBeans() {
        assertNotNull(auditLogPersistenceService);
        assertNotNull(timezoneConfig);
    }
}
