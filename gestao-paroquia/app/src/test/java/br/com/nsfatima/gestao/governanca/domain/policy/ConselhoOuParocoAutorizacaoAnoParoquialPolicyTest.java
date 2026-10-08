package br.com.nsfatima.gestao.governanca.domain.policy;

import br.com.nsfatima.gestao.governanca.domain.exception.AnoParoquialNaoAutorizadoException;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Especifica a autoridade canônica exclusiva do Pároco para mudar o ciclo do Ano Paroquial. */
@DisplayName("Policy: autorização do Ano Paroquial")
class ConselhoOuParocoAutorizacaoAnoParoquialPolicyTest {

    private final ConselhoOuParocoAutorizacaoAnoParoquialPolicy policy =
            new ConselhoOuParocoAutorizacaoAnoParoquialPolicy();

    /** RN-GOV-ANO-01: somente o Pároco detém autoridade canônica para travar ou abrir o calendário anual. */
    @Test
    @DisplayName("permite pároco sem grupo organizacional")
    void permiteParocoSemGrupoOrganizacional() {
        assertThatCode(() -> policy.assertPodeGerir(PapelGlobal.PAROCO.storedValue(), null))
                .doesNotThrowAnyException();
    }

    /** RN-GOV-ANO-02: membros de conselho possuem apenas voto consultivo e não podem travar o ano. */
    @Test
    @DisplayName("nega membro do conselho pastoral e administrador técnico")
    void negaConselhoEAdmin() {
        assertThatThrownBy(() -> policy.assertPodeGerir(
                PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL.storedValue(), TipoOrganizacao.CONSELHO.name()))
                .isInstanceOf(AnoParoquialNaoAutorizadoException.class);
        assertThatThrownBy(() -> policy.assertPodeGerir(PapelGlobal.ADMIN.storedValue(), null))
                .isInstanceOf(AnoParoquialNaoAutorizadoException.class);
    }

    /** RN-GOV-ANO-03: coordenação de pastoral ou movimento não pode travar o planejamento paroquial. */
    @Test
    @DisplayName("nega coordenador pastoral")
    void negaCoordenadorPastoralForaDoConselho() {
        assertThatThrownBy(() -> policy.assertPodeGerir(
                PapelOrganizacional.COORDENADOR.storedValue(), TipoOrganizacao.PASTORAL.name()))
                .isInstanceOf(AnoParoquialNaoAutorizadoException.class);
    }
}
