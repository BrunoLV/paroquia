package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.UsuarioEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApproveUsuarioUseCaseTest {

    private UsuarioJpaRepository usuarioRepository;
    private MembroGrupoJpaRepository vinculoRepository;
    private AprovarUsuarioUseCase useCase;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioJpaRepository.class);
        vinculoRepository = mock(MembroGrupoJpaRepository.class);
        useCase = new AprovarUsuarioUseCase(usuarioRepository, vinculoRepository);
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
        existing.setUsername("pendente.usuario");
        existing.setPasswordHash("hash");
        existing.setEnabled(false);
        UUID fielId = UUID.randomUUID();
        existing.setFielId(fielId);
        when(usuarioRepository.findById(id)).thenReturn(Optional.of(existing));
        when(vinculoRepository.findByFielId(fielId)).thenReturn(Collections.emptyList());

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("paroco.local", "senha"));

        useCase.execute(id);

        ArgumentCaptor<UsuarioEntity> captor = ArgumentCaptor.forClass(UsuarioEntity.class);
        verify(usuarioRepository).save(captor.capture());

        assertThat(captor.getValue().getAtualizadoPor()).isEqualTo("paroco.local");
    }
}
