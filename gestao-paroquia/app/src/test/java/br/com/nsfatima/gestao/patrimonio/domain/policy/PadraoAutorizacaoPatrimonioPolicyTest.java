package br.com.nsfatima.gestao.patrimonio.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
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
        assertThat(policy.isHighLevelActor(PapelOrganizacional.ADMIN)).isTrue();
        assertThat(policy.isHighLevelActor(PapelOrganizacional.PAROCO)).isTrue();
        assertThat(policy.isHighLevelActor(PapelOrganizacional.PRESIDENTE_CPC)).isTrue();
        assertThat(policy.isHighLevelActor(PapelOrganizacional.MESA_CPP)).isTrue();
        assertThat(policy.isHighLevelActor(PapelOrganizacional.COORD_PASTORAL_GLOBAL)).isTrue();
        assertThat(policy.isHighLevelActor(PapelOrganizacional.COORD_PASTORAL_LOCAL)).isFalse();
        assertThat(policy.isHighLevelActor(PapelOrganizacional.MEMBRO_PASTORAL)).isFalse();
    }

    @Test
    @DisplayName("Permite gestão para alto nível mesmo em unidade distinta")
    void devePermitirGestaoParaAltoNivelEmQualquerUnidade() {
        UUID outraUnidade = UUID.randomUUID();
        assertThatCode(() -> policy.assertPodeGerirPatrimonio(
                PapelOrganizacional.PAROCO, outraUnidade, unidadeProprietaria))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.assertPodeGerirPatrimonio(PapelOrganizacional.ADMIN, null, unidadeProprietaria))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Permite coordenação local na própria unidade e rejeita em unidade distinta")
    void deveValidarEscopoDeCoordenacaoLocal() {
        UUID outraUnidade = UUID.randomUUID();
        assertThatCode(() -> policy.assertPodeGerirPatrimonio(
                PapelOrganizacional.COORD_PASTORAL_LOCAL, unidadeProprietaria, unidadeProprietaria))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> policy.assertPodeGerirPatrimonio(
                PapelOrganizacional.COORD_PASTORAL_LOCAL, outraUnidade, unidadeProprietaria))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("outra unidade territorial");
    }

    @Test
    @DisplayName("Rejeita papéis sem permissão de gestão de patrimônio")
    void deveRejeitarPapeisSemPermissao() {
        assertThatThrownBy(() -> policy.assertPodeGerirPatrimonio(
                PapelOrganizacional.MEMBRO_PASTORAL, unidadeProprietaria, unidadeProprietaria))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("não tem permissão");
    }
}
