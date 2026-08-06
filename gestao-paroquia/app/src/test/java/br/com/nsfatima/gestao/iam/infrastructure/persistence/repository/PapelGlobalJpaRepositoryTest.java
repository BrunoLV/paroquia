package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.support.FielTestFixtures;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.PapelGlobalEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
class PapelGlobalJpaRepositoryTest {

    @Autowired
    private PapelGlobalJpaRepository repository;

    @Autowired
    private FielJpaRepository fielRepository;

    @Test
    @DisplayName("Deve salvar e buscar um PapelGlobal por fielId")
    void deveSalvarEBuscarPapelGlobal() {
        UUID fielId = FielTestFixtures.criarFiel(fielRepository);

        PapelGlobalEntity entity = new PapelGlobalEntity();
        entity.setId(UUID.randomUUID());
        entity.setFielId(fielId);
        entity.setPapel("paroco");
        entity.setAtivo(true);
        repository.saveAndFlush(entity);

        var found = repository.findByFielIdAndAtivoTrue(fielId);
        assertThat(found).hasSize(1);
        assertThat(found.getFirst().getPapel()).isEqualTo("paroco");
    }

    @Test
    @DisplayName("Deve falhar ao duplicar (fielId, papel)")
    void deveFalharAoDuplicarFielIdPapel() {
        UUID fielId = FielTestFixtures.criarFiel(fielRepository);

        PapelGlobalEntity primeiro = new PapelGlobalEntity();
        primeiro.setId(UUID.randomUUID());
        primeiro.setFielId(fielId);
        primeiro.setPapel("admin");
        primeiro.setAtivo(true);
        repository.saveAndFlush(primeiro);

        PapelGlobalEntity duplicado = new PapelGlobalEntity();
        duplicado.setId(UUID.randomUUID());
        duplicado.setFielId(fielId);
        duplicado.setPapel("admin");
        duplicado.setAtivo(true);

        assertThatThrownBy(() -> repository.saveAndFlush(duplicado))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
