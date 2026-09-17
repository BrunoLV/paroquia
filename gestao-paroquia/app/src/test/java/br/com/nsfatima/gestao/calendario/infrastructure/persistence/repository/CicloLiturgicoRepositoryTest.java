package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.domain.model.TipoCicloLiturgico;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.CicloLiturgicoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

import br.com.nsfatima.gestao.support.infrastructure.configuration.JpaAuditingConfig;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@DataJpaTest
@Import(JpaAuditingConfig.class)
class CicloLiturgicoRepositoryTest {

    @Autowired
    private CicloLiturgicoRepository repository;

    @Test
    @DisplayName("Deve carregar os ciclos litúrgicos institucionais do baseline Flyway")
    void deveCarregarCiclosLiturgicosDoBaseline() {
        List<CicloLiturgicoEntity> ciclos = repository.findAll();
        assertTrue(ciclos.size() >= 6, "Deve haver pelo menos 6 ciclos litúrgicos semeados no banco");

        Optional<CicloLiturgicoEntity> quaresma = repository.findFirstByTipoCiclo(TipoCicloLiturgico.QUARESMA);
        assertTrue(quaresma.isPresent());
        assertEquals("Quaresma", quaresma.get().getNome());
        assertEquals(2.5, quaresma.get().getFatorDemanda());
    }
}
