package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.AnoLiturgicoEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@DataJpaTest
class AnoLiturgicoRepositoryTest {

    @Autowired
    private AnoLiturgicoRepository repository;

    @Test
    @DisplayName("Deve carregar seed de 10 anos litúrgicos e buscar ano litúrgico por data")
    void deveBuscarAnoLiturgicoPorData() {
        assertThat(repository.count()).isGreaterThanOrEqualTo(10);

        Instant dataDezembro2026 = Instant.parse("2026-12-15T10:00:00Z");
        Optional<AnoLiturgicoEntity> anoLiturgico = repository.findAnoLiturgicoByData(dataDezembro2026);

        assertThat(anoLiturgico).isPresent();
        assertThat(anoLiturgico.get().getAno()).isEqualTo(2027);
        assertThat(anoLiturgico.get().getCicloLetra()).isEqualTo("A");
    }
}
