package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.entity.PapelGlobalEntity;
import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.PapelGlobalJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RevogarPapelGlobalUseCaseTest {

    private PapelGlobalJpaRepository repository;
    private RevogarPapelGlobalUseCase useCase;
    private UUID fielId;

    @BeforeEach
    void setUp() {
        repository = mock(PapelGlobalJpaRepository.class);
        useCase = new RevogarPapelGlobalUseCase(repository);
        fielId = UUID.randomUUID();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "admin", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));
    }

    @Test
    void deveRevogarPapelGlobalAtivo() {
        authenticateAsAdmin();
        PapelGlobalEntity entity = new PapelGlobalEntity();
        entity.setAtivo(true);
        when(repository.findByFielIdAndPapel(fielId, "paroco")).thenReturn(Optional.of(entity));

        useCase.execute(fielId, "PAROCO");

        assertThat(entity.isAtivo()).isFalse();
    }

    @Test
    void deveNegarQuandoAtorNaoEhAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThatThrownBy(() -> useCase.execute(fielId, "PAROCO"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deveLancarExcecaoQuandoNaoPossuiPapel() {
        authenticateAsAdmin();
        when(repository.findByFielIdAndPapel(fielId, "paroco")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(fielId, "PAROCO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoQuandoPapelJaInativo() {
        authenticateAsAdmin();
        PapelGlobalEntity entity = new PapelGlobalEntity();
        entity.setAtivo(false);
        when(repository.findByFielIdAndPapel(fielId, "paroco")).thenReturn(Optional.of(entity));

        assertThatThrownBy(() -> useCase.execute(fielId, "PAROCO"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
