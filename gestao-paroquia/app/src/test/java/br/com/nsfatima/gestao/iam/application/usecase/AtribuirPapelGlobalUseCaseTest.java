package br.com.nsfatima.gestao.iam.application.usecase;

import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository.FielJpaRepository;
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

class AtribuirPapelGlobalUseCaseTest {

    private PapelGlobalJpaRepository repository;
    private FielJpaRepository fielRepository;
    private AtribuirPapelGlobalUseCase useCase;
    private UUID fielId;

    @BeforeEach
    void setUp() {
        repository = mock(PapelGlobalJpaRepository.class);
        fielRepository = mock(FielJpaRepository.class);
        useCase = new AtribuirPapelGlobalUseCase(repository, fielRepository);
        fielId = UUID.randomUUID();
        when(fielRepository.findById(fielId)).thenReturn(Optional.of(new FielEntity()));
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
    void deveConcederPapelGlobalQuandoAtorEhAdmin() {
        authenticateAsAdmin();
        when(repository.findByFielIdAndPapel(fielId, "vigario")).thenReturn(Optional.empty());
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(inv -> inv.getArgument(0));

        UUID id = useCase.execute(fielId, "VIGARIO");

        assertThat(id).isNotNull();
    }

    @Test
    void deveNegarQuandoAtorNaoEhAdmin() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_USER"))));

        assertThatThrownBy(() -> useCase.execute(fielId, "VIGARIO"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deveLancarExcecaoQuandoFielNaoEncontrado() {
        authenticateAsAdmin();
        when(fielRepository.findById(fielId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(fielId, "VIGARIO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoQuandoPapelInvalido() {
        authenticateAsAdmin();

        assertThatThrownBy(() -> useCase.execute(fielId, "COORDENADOR"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveLancarExcecaoQuandoJaPossuiPapelAtivo() {
        authenticateAsAdmin();
        PapelGlobalEntity existente = new PapelGlobalEntity();
        existente.setAtivo(true);
        when(repository.findByFielIdAndPapel(fielId, "vigario")).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> useCase.execute(fielId, "VIGARIO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveReativarPapelDesativado() {
        authenticateAsAdmin();
        PapelGlobalEntity existente = new PapelGlobalEntity();
        existente.setId(UUID.randomUUID());
        existente.setAtivo(false);
        when(repository.findByFielIdAndPapel(fielId, "vigario")).thenReturn(Optional.of(existente));
        when(repository.save(existente)).thenReturn(existente);

        UUID id = useCase.execute(fielId, "VIGARIO");

        assertThat(existente.isAtivo()).isTrue();
        assertThat(id).isEqualTo(existente.getId());
    }
}
