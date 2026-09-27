package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PadraoAutorizacaoCancelamentoEventoPolicyTest {

    private final PadraoAutorizacaoCancelamentoEventoPolicy policy = new PadraoAutorizacaoCancelamentoEventoPolicy();
    private final UUID atorOrgId = UUID.randomUUID();

    @Test
    @DisplayName("Pároco tem cancelamento imediato")
    void deveRetornarModoImediatoParaParoco() {
        assertThat(policy.resolveRequestMode("paroco", null, atorOrgId, atorOrgId))
                .isEqualTo(ModoRequisicaoCancelamento.IMEDIATO);
        assertThat(policy.resolveRequestMode("bispo-diocesano", null, atorOrgId, atorOrgId))
                .isEqualTo(ModoRequisicaoCancelamento.IMEDIATO);
    }

    @Test
    @DisplayName("Vigário requer aprovação para cancelamento")
    void deveRetornarRequerAprovacaoParaVigario() {
        assertThat(policy.resolveRequestMode("vigario", null, atorOrgId, atorOrgId))
                .isEqualTo(ModoRequisicaoCancelamento.REQUER_APROVACAO);
    }

    @Test
    @DisplayName("Liderança local requer aprovação no próprio escopo e falha fora de escopo")
    void deveValidarLiderancaLocal() {
        UUID outraOrgId = UUID.randomUUID();
        assertThat(policy.resolveRequestMode(
                "coordenador", "PASTORAL", atorOrgId, atorOrgId))
                .isEqualTo(ModoRequisicaoCancelamento.REQUER_APROVACAO);

        assertThatThrownBy(() -> policy.resolveRequestMode(
                "coordenador", "PASTORAL", atorOrgId, outraOrgId))
                .isInstanceOf(OperacaoProibidaException.class);
    }

    @Test
    @DisplayName("Apenas Pároco pode decidir aprovações de cancelamento de evento")
    void deveValidarDecisaoDeAprovacao() {
        assertThatCode(() -> policy.assertCanDecideAprovacao("paroco", null))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> policy.assertCanDecideAprovacao("secretario-conselho", "CONSELHO"))
                .isInstanceOf(OperacaoProibidaException.class);

        assertThatThrownBy(() -> policy.assertCanDecideAprovacao(
                "coordenador", "PASTORAL"))
                .isInstanceOf(OperacaoProibidaException.class);
    }
}
