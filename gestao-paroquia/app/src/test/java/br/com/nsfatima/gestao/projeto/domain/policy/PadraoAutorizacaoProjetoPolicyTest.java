package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.exception.OperacaoProibidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PadraoAutorizacaoProjetoPolicyTest {

    private final PadraoAutorizacaoProjetoPolicy policy = new PadraoAutorizacaoProjetoPolicy();

    @Test
    @DisplayName("Pároco e Bispo Diocesano são considerados atores de alto nível; Admin é isolado")
    void deveReconhecerAltoNivelParaParocoEBispoDiocesano() {
        assertThat(policy.isAtorDeAltoNivel("paroco", "PASTORAL")).isTrue();
        assertThat(policy.isAtorDeAltoNivel("bispo-diocesano", null)).isTrue();
        assertThat(policy.isAtorDeAltoNivel("admin", null)).isFalse();
        assertThat(policy.isAtorDeAltoNivel("coordenador", "PASTORAL")).isFalse();
        assertThat(policy.isAtorDeAltoNivel("membro-ativo", "PASTORAL")).isFalse();
    }

    @Test
    @DisplayName("Ator de alto nível pode criar ou editar projeto para qualquer organização")
    void devePermitirCriacaoEEdicaoParaAltoNivelMesmoEmOutraOrganizacao() {
        UUID atorOrg = UUID.randomUUID();
        UUID outraOrg = UUID.randomUUID();

        assertThatCode(() -> policy.assertCanCreate("paroco", "PASTORAL", atorOrg, outraOrg))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.assertCanEdit("bispo-diocesano", null, atorOrg, outraOrg))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Coordenador só pode criar e editar projeto na própria organização")
    void deveBloquearCriacaoEEdicaoDeCoordenadorEmOutraOrganizacao() {
        UUID atorOrg = UUID.randomUUID();
        UUID outraOrg = UUID.randomUUID();

        assertThatCode(() -> policy.assertCanCreate(
                "coordenador", "PASTORAL", atorOrg, atorOrg))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> policy.assertCanCreate(
                "coordenador", "PASTORAL", atorOrg, outraOrg))
                .isInstanceOf(OperacaoProibidaException.class)
                .hasMessageContaining("outra organização");

        assertThatThrownBy(() -> policy.assertCanEdit(
                "coordenador", "PASTORAL", atorOrg, outraOrg))
                .isInstanceOf(OperacaoProibidaException.class)
                .hasMessageContaining("outra organização");
    }

    @Test
    @DisplayName("Transição de projeto exige liderança na própria organização quando não for alto nível")
    void deveValidarTransicoesDeProjetoCorretamente() {
        UUID atorOrg = UUID.randomUUID();
        UUID outraOrg = UUID.randomUUID();

        assertThatCode(() -> policy.assertCanStartOrConclude("paroco", null, atorOrg, outraOrg))
                .doesNotThrowAnyException();
        assertThatCode(() -> policy.assertCanStartOrConclude(
                "coordenador", "MOVIMENTO", atorOrg, atorOrg))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> policy.assertCanStartOrConclude(
                "coordenador", "MOVIMENTO", atorOrg, outraOrg))
                .isInstanceOf(OperacaoProibidaException.class);
        assertThatThrownBy(() -> policy.assertCanStartOrConclude(
                "membro-ativo", "MOVIMENTO", atorOrg, atorOrg))
                .isInstanceOf(OperacaoProibidaException.class);
    }
}
