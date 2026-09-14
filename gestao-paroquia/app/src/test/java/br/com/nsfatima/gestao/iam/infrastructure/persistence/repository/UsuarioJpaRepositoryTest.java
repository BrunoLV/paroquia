package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.fiel.support.FielTestFixtures;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import br.com.nsfatima.gestao.support.infrastructure.configuration.JpaAuditingConfig;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@DataJpaTest
@Import(JpaAuditingConfig.class)
class UsuarioJpaRepositoryTest {

    @Autowired
    private UsuarioJpaRepository repository;

    @Autowired
    private FielJpaRepository fielRepository;

    @Test
    void deveBuscarUsuarioPorUsernameIgnorarCase() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(userId);
        usuario.setUsername("TestUser");
        usuario.setPasswordHash("hash");
        usuario.setEnabled(true);
        usuario.setFielId(FielTestFixtures.criarFiel(fielRepository));
        usuario.setCriadoEmUtc(Instant.now());
        usuario.setAtualizadoEmUtc(Instant.now());
        repository.save(usuario);

        Optional<UsuarioEntity> found = repository.findByUsernameIgnoreCase("testuser");
        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getId());
    }

    @Test
    void deveFalharAoPersistirSemFielIdPoisAgoraEhObrigatorio() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(userId);
        usuario.setUsername("semfiel");
        usuario.setPasswordHash("hash");
        usuario.setEnabled(true);
        usuario.setCriadoEmUtc(Instant.now());
        usuario.setAtualizadoEmUtc(Instant.now());

        assertThatThrownBy(() -> repository.saveAndFlush(usuario))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @WithMockUser(username = "admin")
    void devePersistirComFielIdECamposDeAuditoria() {
        UUID userId = UUID.randomUUID();
        UUID fielId = FielTestFixtures.criarFiel(fielRepository);

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(userId);
        usuario.setUsername("comfiel");
        usuario.setPasswordHash("hash");
        usuario.setEnabled(true);
        usuario.setFielId(fielId);

        UsuarioEntity saved = repository.saveAndFlush(usuario);
        UsuarioEntity found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getFielId()).isEqualTo(fielId);
        assertThat(found.getCriadoEmUtc()).isNotNull();
        assertThat(found.getAtualizadoEmUtc()).isNotNull();
        assertThat(found.getCriadoPor()).isEqualTo("admin");
        assertThat(found.getAtualizadoPor()).isEqualTo("admin");
    }
}
