package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class UsuarioJpaRepositoryTest {

    @Autowired
    private UsuarioJpaRepository repository;

    @Test
    void shouldFindUserByUsernameIgnoreCase() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(userId);
        usuario.setUsername("TestUser");
        usuario.setPasswordHash("hash");
        usuario.setEnabled(true);
        repository.save(usuario);

        Optional<UsuarioEntity> found = repository.findByUsernameIgnoreCase("testuser");
        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getId());
    }
}
