package br.com.nsfatima.gestao.calendario.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
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
    @DisplayName("Pároco e liderança de conselho têm cancelamento imediato")
    void deveRetornarModoImediatoParaParocoEConselho() {
        assertThat(policy.resolveRequestMode(PapelOrganizacional.PAROCO, null, atorOrgId, atorOrgId))
                .isEqualTo(CancelamentoRequestMode.IMMEDIATE);
        assertThat(policy.resolveRequestMode(PapelOrganizacional.PRESIDENTE_CPC, TipoGrupo.CONSELHO, atorOrgId, atorOrgId))
                .isEqualTo(CancelamentoRequestMode.IMMEDIATE);
    }

    @Test
    @DisplayName("Vigário requer aprovação para cancelamento")
    void deveRetornarRequerAprovacaoParaVigario() {
        assertThat(policy.resolveRequestMode(PapelOrganizacional.VIGARIO, null, atorOrgId, atorOrgId))
                .isEqualTo(CancelamentoRequestMode.REQUIRES_APROVACAO);
    }

    @Test
    @DisplayName("Liderança local requer aprovação no próprio escopo e falha fora de escopo")
    void deveValidarLiderancaLocal() {
        UUID outraOrgId = UUID.randomUUID();
        assertThat(policy.resolveRequestMode(PapelOrganizacional.COORD_PASTORAL_LOCAL, TipoGrupo.PASTORAL, atorOrgId, atorOrgId))
                .isEqualTo(CancelamentoRequestMode.REQUIRES_APROVACAO);

        assertThatThrownBy(() -> policy.resolveRequestMode(PapelOrganizacional.COORD_PASTORAL_LOCAL, TipoGrupo.PASTORAL, atorOrgId, outraOrgId))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    @DisplayName("Apenas Pároco e liderança de conselho podem decidir aprovações de evento")
    void deveValidarDecisaoDeAprovacao() {
        assertThatCode(() -> policy.assertCanDecideAprovacao(PapelOrganizacional.PAROCO, null))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.assertCanDecideAprovacao(PapelOrganizacional.PRESIDENTE_CPC, TipoGrupo.CONSELHO))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> policy.assertCanDecideAprovacao(PapelOrganizacional.COORD_PASTORAL_LOCAL, TipoGrupo.PASTORAL))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
