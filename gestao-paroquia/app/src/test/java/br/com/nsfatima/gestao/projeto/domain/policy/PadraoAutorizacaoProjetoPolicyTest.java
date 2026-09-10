package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.ForbiddenOperationException;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PadraoAutorizacaoProjetoPolicyTest {

    private final PadraoAutorizacaoProjetoPolicy policy = new PadraoAutorizacaoProjetoPolicy();

    @Test
    @DisplayName("Pároco e Admin são sempre considerados atores de alto nível")
    void deveReconhecerAltoNivelParaParocoEAdmin() {
        assertThat(policy.isHighLevelActor(PapelOrganizacional.PAROCO, TipoGrupo.PASTORAL)).isTrue();
        assertThat(policy.isHighLevelActor(PapelOrganizacional.ADMIN, null)).isTrue();
        assertThat(policy.isHighLevelActor(PapelOrganizacional.PRESIDENTE_CPC, TipoGrupo.CONSELHO)).isTrue();
        assertThat(policy.isHighLevelActor(PapelOrganizacional.PRESIDENTE_CPC, TipoGrupo.PASTORAL)).isFalse();
        assertThat(policy.isHighLevelActor(PapelOrganizacional.MEMBRO_PASTORAL, TipoGrupo.PASTORAL)).isFalse();
    }

    @Test
    @DisplayName("Ator de alto nível pode criar ou editar projeto para qualquer organização")
    void devePermitirCriacaoEEdicaoParaAltoNivelMesmoEmOutraOrganizacao() {
        UUID atorOrg = UUID.randomUUID();
        UUID outraOrg = UUID.randomUUID();

        assertThatCode(() -> policy.assertCanCreate(PapelOrganizacional.PAROCO, TipoGrupo.PASTORAL, atorOrg, outraOrg))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.assertCanEdit(PapelOrganizacional.ADMIN, null, atorOrg, outraOrg))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Coordenador só pode criar e editar projeto na própria organização")
    void deveBloquearCriacaoEEdicaoDeCoordenadorEmOutraOrganizacao() {
        UUID atorOrg = UUID.randomUUID();
        UUID outraOrg = UUID.randomUUID();

        assertThatCode(() -> policy.assertCanCreate(PapelOrganizacional.COORD_PASTORAL, TipoGrupo.PASTORAL, atorOrg, atorOrg))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> policy.assertCanCreate(PapelOrganizacional.COORD_PASTORAL, TipoGrupo.PASTORAL, atorOrg, outraOrg))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("outra organização");

        assertThatThrownBy(() -> policy.assertCanEdit(PapelOrganizacional.COORD_PASTORAL, TipoGrupo.PASTORAL, atorOrg, outraOrg))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("outra organização");
    }

    @Test
    @DisplayName("Transição de projeto exige liderança na própria organização quando não for alto nível")
    void deveValidarTransicoesDeProjetoCorretamente() {
        UUID atorOrg = UUID.randomUUID();
        UUID outraOrg = UUID.randomUUID();

        assertThatCode(() -> policy.assertCanStartOrConclude(PapelOrganizacional.PAROCO, null, atorOrg, outraOrg))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.assertCanStartOrConclude(PapelOrganizacional.COORD_MOVIMENTO, TipoGrupo.MOVIMENTO, atorOrg, atorOrg))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> policy.assertCanStartOrConclude(PapelOrganizacional.COORD_MOVIMENTO, TipoGrupo.MOVIMENTO, atorOrg, outraOrg))
                .isInstanceOf(ForbiddenOperationException.class);
        assertThatThrownBy(() -> policy.assertCanStartOrConclude(PapelOrganizacional.MEMBRO_MOVIMENTO, TipoGrupo.MOVIMENTO, atorOrg, atorOrg))
                .isInstanceOf(ForbiddenOperationException.class);
    }
}
