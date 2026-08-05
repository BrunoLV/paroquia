package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateUsuarioUseCaseTest {

    private UsuarioJpaRepository usuarioRepository;
    private CreateUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");
        useCase = new CreateUsuarioUseCase(usuarioRepository, passwordEncoder);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void devePopularAuditoriaComOActorAutenticado() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin.paroquia", "senha"));
        when(usuarioRepository.findByUsernameIgnoreCase("novo.usuario")).thenReturn(Optional.empty());
        when(usuarioRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        useCase.execute("novo.usuario", "senhaForte");

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());

        UsuarioEntity saved = captor.getValue();
        assertThat(saved.getCriadoPor()).isEqualTo("admin.paroquia");
        assertThat(saved.getAtualizadoPor()).isEqualTo("admin.paroquia");
    }
}
