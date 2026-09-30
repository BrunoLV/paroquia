package br.com.nsfatima.gestao.patrimonio.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PadraoAutorizacaoPatrimonioPolicyTest {

    private final PadraoAutorizacaoPatrimonioPolicy policy = new PadraoAutorizacaoPatrimonioPolicy();
    private final UUID unidadeProprietaria = UUID.randomUUID();

    @Test
    @DisplayName("Identifica papéis de alto nível com alçada ampla")
    void deveIdentificarAltoNivel() {
        assertThat(policy.isAtorDeAltoNivel(PapelGlobal.PAROCO.storedValue())).isTrue();
        assertThat(policy.isAtorDeAltoNivel(PapelGlobal.BISPO_DIOCESANO.storedValue())).isTrue();
        assertThat(policy.isAtorDeAltoNivel(PapelOrganizacional.MEMBRO_CONSELHO_ECONOMICO.storedValue())).isTrue();
        assertThat(policy.isAtorDeAltoNivel(PapelGlobal.ADMIN.storedValue())).isFalse();
        assertThat(policy.isAtorDeAltoNivel(PapelOrganizacional.COORDENADOR.storedValue())).isFalse();
        assertThat(policy.isAtorDeAltoNivel(PapelOrganizacional.MEMBRO_ATIVO.storedValue())).isFalse();
    }

    @Test
    @DisplayName("Permite gestão para alto nível mesmo em unidade distinta")
    void devePermitirGestaoParaAltoNivelEmQualquerUnidade() {
        UUID outraUnidade = UUID.randomUUID();
        assertThatCode(() -> policy.assertPodeGerirPatrimonio(
                PapelGlobal.PAROCO.storedValue(), outraUnidade, unidadeProprietaria))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.assertPodeGerirPatrimonio(
                PapelOrganizacional.MEMBRO_CONSELHO_ECONOMICO.storedValue(), null, unidadeProprietaria))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Permite coordenação na própria unidade e rejeita em unidade distinta")
    void deveValidarEscopoDeCoordenacaoLocal() {
        UUID outraUnidade = UUID.randomUUID();
        assertThatCode(() -> policy.assertPodeGerirPatrimonio(
                PapelOrganizacional.COORDENADOR.storedValue(), unidadeProprietaria, unidadeProprietaria))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> policy.assertPodeGerirPatrimonio(
                PapelOrganizacional.COORDENADOR.storedValue(), outraUnidade, unidadeProprietaria))
                .isInstanceOf(OperacaoProibidaException.class)
                .hasMessageContaining("outra unidade territorial");
    }

    @Test
    @DisplayName("Rejeita papéis sem permissão de gestão de patrimônio")
    void deveRejeitarPapeisSemPermissao() {
        assertThatThrownBy(() -> policy.assertPodeGerirPatrimonio(
                PapelOrganizacional.MEMBRO_ATIVO.storedValue(), unidadeProprietaria, unidadeProprietaria))
                .isInstanceOf(OperacaoProibidaException.class)
                .hasMessageContaining("não tem permissão");
    }
}
