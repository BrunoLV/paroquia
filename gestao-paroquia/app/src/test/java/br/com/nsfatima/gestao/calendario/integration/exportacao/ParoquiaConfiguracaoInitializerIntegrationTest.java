package br.com.nsfatima.gestao.calendario.integration.exportacao;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.ParoquiaConfiguracaoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ParoquiaConfiguracaoInitializerIntegrationTest {

    @Autowired
    private ParoquiaConfiguracaoRepository repository;

    @Test
    void deveSemearPadraoConfigurationEmInicializacao() {
        // The initializer runs automatically on application startup, so the config must exist.
        List<ParoquiaConfiguracaoEntity> configs = repository.findAll();
        assertFalse(configs.isEmpty(), "Should have initialized at least one configuration");

        ParoquiaConfiguracaoEntity config = configs.getFirst();
        assertNotNull(config.getId());
        assertEquals("Paróquia Nossa Senhora de Fátima", config.getNome());
        assertEquals("Diocese de São Miguel Paulista", config.getDiocese());
        assertEquals("Rua Gijoca, 7 - Vila Curuçá Velha - São Paulo/SP", config.getEndereco());
        assertEquals("#1A237E", config.getCorPrimariaHex());
        assertEquals("#808080", config.getCorSecundariaHex());
        assertNotNull(config.getLogoBytes());
        assertTrue(config.getLogoBytes().length > 0);
    }
}
