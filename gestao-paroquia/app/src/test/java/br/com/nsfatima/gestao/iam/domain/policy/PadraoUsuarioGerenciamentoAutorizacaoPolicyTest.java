package br.com.nsfatima.gestao.iam.domain.policy;

import br.com.nsfatima.gestao.iam.domain.exception.GerenciamentoUsuarioNegadoException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PadraoUsuarioGerenciamentoAutorizacaoPolicyTest {

    private final UsuarioGerenciamentoAutorizacaoPolicy policy = new PadraoUsuarioGerenciamentoAutorizacaoPolicy();

    @Test
    void adminRedefineSenhaDeQualquerAlvoInclusiveComPapelGlobal() {
        assertThatCode(() -> policy.assertPodeRedefinirSenha(true, false, true)).doesNotThrowAnyException();
    }

    @Test
    void parocoRedefineSenhaDeAlvoSemPapelGlobal() {
        assertThatCode(() -> policy.assertPodeRedefinirSenha(false, true, false)).doesNotThrowAnyException();
    }

    @Test
    void parocoNaoRedefineSenhaDeAlvoComPapelGlobal() {
        assertThatThrownBy(() -> policy.assertPodeRedefinirSenha(false, true, true))
                .isInstanceOf(GerenciamentoUsuarioNegadoException.class);
    }

    @Test
    void atorSemAlcadaDeAdminOuParocoNaoRedefineSenha() {
        assertThatThrownBy(() -> policy.assertPodeRedefinirSenha(false, false, false))
                .isInstanceOf(GerenciamentoUsuarioNegadoException.class);
    }

    @Test
    void negaQuandoAtorNaoCoordenaNenhumaOrganizacao() {
        assertThatThrownBy(() -> policy.assertPodeGerenciarAlvo(List.of(), List.of(UUID.randomUUID())))
                .isInstanceOf(GerenciamentoUsuarioNegadoException.class);
    }

    @Test
    void negaQuandoNaoHaOrganizacaoEmComumComOAlvo() {
        assertThatThrownBy(() -> policy.assertPodeGerenciarAlvo(
                        List.of(UUID.randomUUID()), List.of(UUID.randomUUID())))
                .isInstanceOf(GerenciamentoUsuarioNegadoException.class);
    }

    @Test
    void permiteQuandoCompartilhaOrganizacaoCoordenada() {
        UUID organizacao = UUID.randomUUID();
        assertThatCode(() -> policy.assertPodeGerenciarAlvo(List.of(organizacao), List.of(organizacao)))
                .doesNotThrowAnyException();
    }

    @Test
    void negaQuandoAtorNaoCoordenaAOrganizacao() {
        assertThatThrownBy(() -> policy.assertECoordenadorDaOrganizacao(false))
                .isInstanceOf(GerenciamentoUsuarioNegadoException.class);
    }
}
