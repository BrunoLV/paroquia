package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UpdateUsuarioUseCaseTest {

    private UsuarioJpaRepository usuarioRepository;
    private AtualizarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        useCase = new AtualizarUsuarioUseCase(usuarioRepository);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devePopularAtualizadoPorComOActorAutenticado() {
        UUID id = UUID.randomUUID();
        UsuarioEntity existing = new UsuarioEntity();
        existing.setId(id);
        existing.setUsername("antigo.username");
        existing.setPasswordHash("hash");
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existing));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin.paroquia", "senha"));

        useCase.execute(id, "novo.username", true);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());

        assertThat(captor.getValue().getAtualizadoPor()).isEqualTo("admin.paroquia");
    }
}
