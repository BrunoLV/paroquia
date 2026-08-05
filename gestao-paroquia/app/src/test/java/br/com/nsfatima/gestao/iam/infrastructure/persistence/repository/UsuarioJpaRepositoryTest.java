package br.com.nsfatima.gestao.iam.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
@DataJpaTest
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
        usuario.setCriadoEmUtc(Instant.now());
        usuario.setAtualizadoEmUtc(Instant.now());
        repository.save(usuario);

        Optional<UsuarioEntity> found = repository.findByUsernameIgnoreCase("testuser");
        assertTrue(found.isPresent());
        assertEquals(userId, found.get().getId());
    }

    @Test
    void devePersistirSemFielIdPoisEhNullable() {
        UUID userId = UUID.randomUUID();
        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(userId);
        usuario.setUsername("semfiel");
        usuario.setPasswordHash("hash");
        usuario.setEnabled(true);
        usuario.setCriadoEmUtc(Instant.now());
        usuario.setAtualizadoEmUtc(Instant.now());

        UsuarioEntity saved = repository.saveAndFlush(usuario);

        assertThat(saved.getFielId()).isNull();
    }

    @Test
    void devePersistirComFielIdECamposDeAuditoria() {
        UUID userId = UUID.randomUUID();
        UUID fielId = criarFiel();
        Instant criadoEm = Instant.now();
        Instant atualizadoEm = criadoEm.plusSeconds(60);

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(userId);
        usuario.setUsername("comfiel");
        usuario.setPasswordHash("hash");
        usuario.setEnabled(true);
        usuario.setFielId(fielId);
        usuario.setCriadoEmUtc(criadoEm);
        usuario.setAtualizadoEmUtc(atualizadoEm);
        usuario.setCriadoPor("admin");
        usuario.setAtualizadoPor("admin");

        UsuarioEntity saved = repository.saveAndFlush(usuario);
        UsuarioEntity found = repository.findById(saved.getId()).orElseThrow();

        assertThat(found.getFielId()).isEqualTo(fielId);
        assertThat(found.getCriadoEmUtc()).isEqualTo(criadoEm);
        assertThat(found.getAtualizadoEmUtc()).isEqualTo(atualizadoEm);
        assertThat(found.getCriadoPor()).isEqualTo("admin");
        assertThat(found.getAtualizadoPor()).isEqualTo("admin");
    }

    private UUID criarFiel() {
        FielEntity fiel = new FielEntity();
        fiel.setId(UUID.randomUUID());
        fiel.setNome("Fiel de Teste");
        fiel.setCpf(UUID.randomUUID().toString().substring(0, 11));
        fiel.setDataNascimento(LocalDate.of(1990, 1, 1));
        fiel.setEmail("fiel.teste@example.com");
        fiel.setTelefone("11999999999");
        fiel.setEndereco("Rua de Teste, 1");
        fiel.setVocacao(Vocacao.LEIGO);
        fiel.setAtivo(true);
        fiel.setCriadoEmUtc(Instant.now());
        fiel.setAtualizadoEmUtc(Instant.now());
        fiel.setCriadoPor("teste");
        fiel.setAtualizadoPor("teste");
        return fielRepository.saveAndFlush(fiel).getId();
    }
}
