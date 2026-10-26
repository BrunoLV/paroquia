package br.com.nsfatima.gestao.iam.domain.policy;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

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
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void atorSemAlcadaDeAdminOuParocoNaoRedefineSenha() {
        assertThatThrownBy(() -> policy.assertPodeRedefinirSenha(false, false, false))
                .isInstanceOf(AccessDeniedException.class);
    }
}
