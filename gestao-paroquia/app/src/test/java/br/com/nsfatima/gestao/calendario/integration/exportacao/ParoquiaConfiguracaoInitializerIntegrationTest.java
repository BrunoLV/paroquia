package br.com.nsfatima.gestao.calendario.integration.exportacao;

import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
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
        List<ParoquiaConfiguracaoEntity> configs = repository.findAll();
        assertFalse(configs.isEmpty(), "Should have initialized at least one configuration");

        ParoquiaConfiguracaoEntity config = configs.getFirst();
        assertNotNull(config.getId());
        assertEquals("#1A237E", config.getCorPrimariaHex());
        assertEquals("#808080", config.getCorSecundariaHex());

        assertNotNull(config.getLogoFundoClaroBytes(), "Logo de fundo claro deve ter sido semeado");
        assertTrue(config.getLogoFundoClaroBytes().length > 0);
        assertEquals("image/png", config.getLogoFundoClaroTipoMime());

        assertNotNull(config.getLogoFundoEscuroBytes(), "Logo de fundo escuro deve ter sido semeado");
        assertTrue(config.getLogoFundoEscuroBytes().length > 0);
        assertEquals("image/png", config.getLogoFundoEscuroTipoMime());

        assertNotNull(config.getLogoBytesPorVariante(VarianteFundoLogo.CLARO));
        assertNotNull(config.getLogoBytesPorVariante(VarianteFundoLogo.ESCURO));
    }
}
