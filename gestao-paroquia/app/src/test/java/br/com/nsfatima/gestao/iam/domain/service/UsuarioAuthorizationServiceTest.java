package br.com.nsfatima.gestao.iam.domain.service;

import br.com.nsfatima.gestao.iam.infrastructure.persistence.repository.UsuarioJpaRepository;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.repository.MembroGrupoJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class UsuarioAuthorizationServiceTest {

    private UsuarioAuthorizationService service;

    @BeforeEach
    void setUp() {
        service = new UsuarioAuthorizationService(
                mock(MembroGrupoJpaRepository.class), mock(UsuarioJpaRepository.class));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateWith(String... roles) {
        var authorities = List.of(roles).stream().map(SimpleGrantedAuthority::new).toList();
        var principal = new UsuarioDetails(UUID.randomUUID(), "actor", "pass", true, "", List.of());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    @Test
    void devePermitirParocoVigarioMesaCppOuAdmin() {
        authenticateWith("ROLE_ADMIN");
        assertThatCode(() -> service.requireParocoVigarioMesaCppOuAdmin()).doesNotThrowAnyException();

        authenticateWith("ROLE_CLERO_PAROCO");
        assertThatCode(() -> service.requireParocoVigarioMesaCppOuAdmin()).doesNotThrowAnyException();

        authenticateWith("ROLE_CLERO_VIGARIO");
        assertThatCode(() -> service.requireParocoVigarioMesaCppOuAdmin()).doesNotThrowAnyException();

        authenticateWith("ROLE_CPP_MESA_CPP");
        assertThatCode(() -> service.requireParocoVigarioMesaCppOuAdmin()).doesNotThrowAnyException();
    }

    @Test
    void deveNegarParaPapelNaoAutorizado() {
        authenticateWith("ROLE_USER");

        assertThatThrownBy(() -> service.requireParocoVigarioMesaCppOuAdmin())
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deveNegarQuandoNaoAutenticado() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(() -> service.requireParocoVigarioMesaCppOuAdmin())
                .isInstanceOf(AccessDeniedException.class);
    }
}
