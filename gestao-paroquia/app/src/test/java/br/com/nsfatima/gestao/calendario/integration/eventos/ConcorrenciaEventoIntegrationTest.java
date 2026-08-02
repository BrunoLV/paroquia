package br.com.nsfatima.gestao.calendario.integration.eventos;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConcorrenciaEventoIntegrationTest {

    @Test
    void deveExporVersionCampo() {
        BaseVersionedEntity entity = new BaseVersionedEntity() {
        };
        assertNotNull(entity.getVersion());
    }
}
