package br.com.nsfatima.gestao.iam.infrastructure.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UsuarioDetailsTest {

    @Test
    void deveIncluirAuthorityRoleGlobalParaCadaPapelGlobalAtivo() {
        UsuarioDetails details = new UsuarioDetails(
                UUID.randomUUID(), "vigario.teste", "hash", true, "",
                List.of(), List.of("vigario", "secretaria-paroquial"));

        List<String> authorities = details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).contains("ROLE_GLOBAL_VIGARIO", "ROLE_GLOBAL_SECRETARIA_PAROQUIAL");
    }

    @Test
    void naoDeveQuebrarQuandoPapeisGlobaisForNuloOuVazio() {
        UsuarioDetails details = new UsuarioDetails(
                UUID.randomUUID(), "sem.papel", "hash", true, "ROLE_USER", List.of());

        List<String> authorities = details.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        assertThat(authorities).containsExactly("ROLE_USER");
    }
}
