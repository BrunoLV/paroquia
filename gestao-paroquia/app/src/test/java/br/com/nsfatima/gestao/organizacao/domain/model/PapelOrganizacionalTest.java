package br.com.nsfatima.gestao.organizacao.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PapelOrganizacionalTest {

    @Test
    @DisplayName("Should resolve all 7 canonical organizational stored values")
    void deveResolverTodosArmazenadoValoresCanonicos() {
        assertThat(PapelOrganizacional.fromStoredValue("secretario-conselho"))
                .isEqualTo(PapelOrganizacional.SECRETARIO_CONSELHO);
        assertThat(PapelOrganizacional.fromStoredValue("membro-conselho-pastoral"))
                .isEqualTo(PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL);
        assertThat(PapelOrganizacional.fromStoredValue("membro-conselho-economico"))
                .isEqualTo(PapelOrganizacional.MEMBRO_CONSELHO_ECONOMICO);
        assertThat(PapelOrganizacional.fromStoredValue("coordenador"))
                .isEqualTo(PapelOrganizacional.COORDENADOR);
        assertThat(PapelOrganizacional.fromStoredValue("secretario-grupo"))
                .isEqualTo(PapelOrganizacional.SECRETARIO_GRUPO);
        assertThat(PapelOrganizacional.fromStoredValue("membro-ativo"))
                .isEqualTo(PapelOrganizacional.MEMBRO_ATIVO);
        assertThat(PapelOrganizacional.fromStoredValue("voluntario"))
                .isEqualTo(PapelOrganizacional.VOLUNTARIO);
        assertThat(PapelOrganizacional.COORDENADOR.getDescricao())
                .isEqualTo("Coordenador");
    }

    @Test
    @DisplayName("Should return null for global or legacy/unknown stored values")
    void deveRetornarNuloParaDesconhecidoOuLegadoArmazenadoValores() {
        assertThat(PapelOrganizacional.fromStoredValue("admin")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue("paroco")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue("coord-pastoral-local")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue("presidente-cpc")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue("vice-coordenador")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue("membro")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue("invalid")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue(null)).isNull();
    }
}
