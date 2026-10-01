package br.com.nsfatima.gestao.sacramento.domain;

import br.com.nsfatima.gestao.sacramento.domain.model.OperacaoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.domain.policy.AlcadaSacramentalPolicy;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlcadaSacramentalPolicyTest {

    private final AlcadaSacramentalPolicy policy = new AlcadaSacramentalPolicy();

    @Test
    void secretariaPrepara_masNaoSela() {
        assertThat(policy.permitida("SECRETARIA_PAROQUIAL", OperacaoSacramental.LAVRAR_RASCUNHO,
                Visibilidade.COMUM)).isTrue();
        assertThat(policy.permitida("SECRETARIA_PAROQUIAL", OperacaoSacramental.HOMOLOGAR,
                Visibilidade.COMUM)).isFalse();
        assertThat(policy.permitida("SECRETARIA_PAROQUIAL", OperacaoSacramental.RETIFICAR,
                Visibilidade.COMUM)).isFalse();
        assertThat(policy.permitida("SECRETARIA_PAROQUIAL", OperacaoSacramental.CANCELAR,
                Visibilidade.COMUM)).isFalse();
    }

    @Test
    void parocoHomologa() {
        assertThat(policy.permitida("PAROCO", OperacaoSacramental.HOMOLOGAR, Visibilidade.COMUM)).isTrue();
    }

    @Test
    void adminNuncaAtuaEmFePublica() {
        for (OperacaoSacramental op : OperacaoSacramental.values()) {
            assertThat(policy.permitida("ADMIN", op, Visibilidade.COMUM)).isFalse();
        }
    }

    @Test
    void livroReservadoVedadoASecretaria() {
        assertThat(policy.permitida("SECRETARIA_PAROQUIAL", OperacaoSacramental.LER, Visibilidade.RESERVADO)).isFalse();
        assertThat(policy.permitida("PAROCO", OperacaoSacramental.LER, Visibilidade.RESERVADO)).isTrue();
        assertThat(policy.permitida("BISPO_DIOCESANO", OperacaoSacramental.LER, Visibilidade.RESERVADO)).isTrue();
        assertThat(policy.acessaLivroReservado("SECRETARIA_PAROQUIAL")).isFalse();
    }

    @Test
    void secretariaVinculaMasSoParocoAprovaRejeitaEDesvincula() {
        assertThat(policy.permitida("SECRETARIA_PAROQUIAL", OperacaoSacramental.VINCULAR, Visibilidade.COMUM)).isTrue();
        assertThat(policy.permitida("SECRETARIA_PAROQUIAL", OperacaoSacramental.APROVAR_VINCULO, Visibilidade.COMUM))
                .isFalse();
        assertThat(policy.permitida("SECRETARIA_PAROQUIAL", OperacaoSacramental.DESVINCULAR, Visibilidade.COMUM))
                .isFalse();
        assertThat(policy.permitida("PAROCO", OperacaoSacramental.APROVAR_VINCULO, Visibilidade.COMUM)).isTrue();
        assertThat(policy.permitida("PAROCO", OperacaoSacramental.DESVINCULAR, Visibilidade.COMUM)).isTrue();
        assertThat(policy.permitida("BISPO_DIOCESANO", OperacaoSacramental.VINCULAR, Visibilidade.COMUM)).isFalse();
    }
}
