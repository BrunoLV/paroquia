package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.api.v1.dto.RegistrarUsuarioRequest;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.OrganizacaoEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.OrganizacaoJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RegistrarUsuarioUseCaseTest {

    private UsuarioJpaRepository usuarioRepository;
    private OrganizacaoJpaRepository organizacaoRepository;
    private RegistrarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        organizacaoRepository = mock(OrganizacaoJpaRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");

        useCase = new RegistrarUsuarioUseCase(
                usuarioRepository,
                organizacaoRepository,
                passwordEncoder
        );
    }

    @Test
    void shouldThrowExceptionWhenIgrejaIdIsNull() {
        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "João Silva",
                "joao@example.com",
                "password123",
                "11999999999",
                LocalDate.of(1990, 1, 1),
                null
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenIgrejaNotFound() {
        UUID igrejaId = UUID.randomUUID();
        when(organizacaoRepository.findById(igrejaId)).thenReturn(Optional.empty());

        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "João Silva",
                "joao@example.com",
                "password123",
                "11999999999",
                LocalDate.of(1990, 1, 1),
                igrejaId
        );

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(request));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void shouldRegisterUserSuccessfullyWithValidIgrejaId() {
        UUID igrejaId = UUID.randomUUID();
        OrganizacaoEntity igrejaEntity = new OrganizacaoEntity();
        igrejaEntity.setId(igrejaId);
        igrejaEntity.setNome("Matriz Nossa Senhora de Fátima");
        igrejaEntity.setTipo(TipoOrganizacao.MATRIZ);

        when(organizacaoRepository.findById(igrejaId)).thenReturn(Optional.of(igrejaEntity));
        when(usuarioRepository.findByUsernameIgnoreCase("joao@example.com")).thenReturn(Optional.empty());

        RegistrarUsuarioRequest request = new RegistrarUsuarioRequest(
                "João Silva",
                "joao@example.com",
                "password123",
                "11999999999",
                LocalDate.of(1990, 1, 1),
                igrejaId
        );

        UUID userId = useCase.execute(request);
        assertNotNull(userId);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());

        UsuarioEntity saved = captor.getValue();
        assertEquals("João Silva", saved.getNome());
        assertEquals("joao@example.com", saved.getUsername());
        assertEquals(igrejaId, saved.getIgrejaId());
    }
}
