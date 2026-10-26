package br.com.nsfatima.gestao.calendario.infrastructure.security;

import br.com.nsfatima.gestao.iam.application.service.PapelGlobalAuthorizationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class EventoActorContextResolverFailClosedTest {

    private final EventoActorContextResolver resolver =
            new EventoActorContextResolver(List.of(), mock(PapelGlobalAuthorizationService.class));

    @AfterEach
    void limpar() {
        SecurityContextHolder.clearContext();
    }

    private void autenticarComPrincipalSimples(String... authorities) {
        List<SimpleGrantedAuthority> concedidas = Arrays.stream(authorities)
                .map(SimpleGrantedAuthority::new).toList();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("desconhecido", null, concedidas));
    }

    @Test
    void principalDesconhecidoSemPapelReconhecidoNaoViraCoordenador() {
        autenticarComPrincipalSimples("ROLE_USER");

        assertThatThrownBy(resolver::resolveRequired).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void principalSemNenhumaAuthorityEhNegado() {
        autenticarComPrincipalSimples();

        assertThatThrownBy(resolver::resolveRequired).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void principalSimplesComPapelGlobalReconhecidoContinuaResolvendo() {
        autenticarComPrincipalSimples("ROLE_PAROCO");

        assertThat(resolver.resolveRequired().role()).isEqualTo("paroco");
    }
}
