package br.com.nsfatima.gestao.iam.domain.policy;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.nsfatima.gestao.iam.domain.exception.GerenciamentoUsuarioNegadoException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class PadraoUsuarioGerenciamentoAutorizacaoPolicyTest {

    private final PadraoUsuarioGerenciamentoAutorizacaoPolicy policy =
            new PadraoUsuarioGerenciamentoAutorizacaoPolicy();

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
