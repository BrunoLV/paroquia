package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ParoquiaConfiguracaoEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ParoquiaConfiguracaoJpaRepositoryTest {

    @Autowired
    private ParoquiaConfiguracaoRepository repository;

    @Test
    void deveSalvarERecuperarConfiguration() {
        ParoquiaConfiguracaoEntity entity = new ParoquiaConfiguracaoEntity();
        UUID id = UUID.randomUUID();
        entity.setId(id);
        entity.setNome("Paróquia Teste");
        entity.setDiocese("Diocese Teste");
        entity.setEndereco("Rua Teste, 123");
        entity.setCorPrimariaHex("#112233");
        entity.setCorSecundariaHex("#445566");
        entity.setLogoBytes(new byte[] {1, 2, 3});

        repository.save(entity);

        ParoquiaConfiguracaoEntity found = repository.findById(id).orElse(null);
        assertNotNull(found);
        assertEquals("Paróquia Teste", found.getNome());
        assertEquals("Diocese Teste", found.getDiocese());
        assertEquals("Rua Teste, 123", found.getEndereco());
        assertEquals("#112233", found.getCorPrimariaHex());
        assertEquals("#445566", found.getCorSecundariaHex());
        assertNotNull(found.getLogoBytes());
        assertEquals(3, found.getLogoBytes().length);
    }
}
