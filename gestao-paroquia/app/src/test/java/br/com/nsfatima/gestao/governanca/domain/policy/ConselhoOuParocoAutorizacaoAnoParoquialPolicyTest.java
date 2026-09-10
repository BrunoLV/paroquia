package br.com.nsfatima.gestao.governanca.domain.policy;

import br.com.nsfatima.gestao.governanca.domain.exception.AnoParoquialNaoAutorizadoException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Especifica a autoridade canônica exigida para mudar o ciclo do Ano Paroquial. */
@DisplayName("Policy: autorização do Ano Paroquial")
class ConselhoOuParocoAutorizacaoAnoParoquialPolicyTest {

    private final ConselhoOuParocoAutorizacaoAnoParoquialPolicy policy =
            new ConselhoOuParocoAutorizacaoAnoParoquialPolicy();

    /** RN-GOV-ANO-01: pároco e administrador mantêm autoridade global sobre o calendário anual. */
    @Test
    @DisplayName("permite pároco sem grupo organizacional")
    void permiteParocoSemGrupoOrganizacional() {
        assertThatCode(() -> policy.assertPodeGerir(PapelOrganizacional.PAROCO, null))
                .doesNotThrowAnyException();
    }

    /** RN-GOV-ANO-02: liderança canônica só exerce a competência dentro de conselho. */
    @Test
    @DisplayName("permite presidente do CPC no conselho")
    void permitePresidenteDoCpcNoConselho() {
        assertThatCode(() -> policy.assertPodeGerir(PapelOrganizacional.PRESIDENTE_CPC, TipoGrupo.CONSELHO))
                .doesNotThrowAnyException();
    }

    /** RN-GOV-ANO-03: coordenação de outro grupo não pode travar o planejamento paroquial. */
    @Test
    @DisplayName("nega coordenador pastoral fora do conselho")
    void negaCoordenadorPastoralForaDoConselho() {
        assertThatThrownBy(() -> policy.assertPodeGerir(PapelOrganizacional.COORD_PASTORAL_GLOBAL, TipoGrupo.PASTORAL))
                .isInstanceOf(AnoParoquialNaoAutorizadoException.class);
    }
}
