package br.com.nsfatima.gestao.reserva.infrastructure.security;

import br.com.nsfatima.gestao.iam.infrastructure.security.LeitorVinculoExterno.Vinculo;
import br.com.nsfatima.gestao.iam.infrastructure.security.UsuarioDetails;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservaSecurityAdapterTest {

    private final ReservaSecurityAdapter adapter = new ReservaSecurityAdapter();

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Sem autenticação no contexto o acesso é negado, sem ator privilegiado padrão")
    void deveNegarQuandoSemAutenticacao() {
        SecurityContextHolder.clearContext();

        assertThatThrownBy(adapter::obterAtorAutenticado).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Principal que não é UsuarioDetails também é negado")
    void deveNegarQuandoPrincipalDesconhecido() {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("usuario-generico", "senha");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(adapter::obterAtorAutenticado).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @DisplayName("Deve montar ator a partir de UsuarioDetails resolvendo authorities e organizações de coordenação")
    void deveMontarAtorAPartirDeUsuarioDetails() {
        UUID usuarioId = UUID.randomUUID();
        UUID orgCoordId = UUID.randomUUID();
        UUID orgMembroId = UUID.randomUUID();

        List<Vinculo> vinculos = List.of(
                new Vinculo(orgCoordId, "PASTORAL", "COORDENADOR"),
                new Vinculo(orgMembroId, "PASTORAL", "MEMBRO"));

        UsuarioDetails details = new UsuarioDetails(
                usuarioId, "maria.silva", "hash", true, "FIEL", vinculos);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        UsuarioActor ator = adapter.obterAtorAutenticado();

        assertThat(ator.id()).isEqualTo(usuarioId);
        assertThat(ator.nome()).isEqualTo("maria.silva");
        assertThat(ator.organizacaoIdsOndeECoordenador()).containsExactly(orgCoordId);
        assertThat(ator.organizacaoIdsOndeEMembro()).containsExactlyInAnyOrder(orgCoordId, orgMembroId);
    }

    @Test
    @DisplayName("Deve retornar ator sem coordenações quando usuário não possui vínculos")
    void deveMontarAtorSemVinculos() {
        UUID usuarioId = UUID.randomUUID();
        UsuarioDetails details = new UsuarioDetails(usuarioId, "joao", "hash", true, "FIEL", null);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        UsuarioActor ator = adapter.obterAtorAutenticado();

        assertThat(ator.organizacaoIdsOndeECoordenador()).isEmpty();
        assertThat(ator.organizacaoIdsOndeEMembro()).isEmpty();
    }
}
