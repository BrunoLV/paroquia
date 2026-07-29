package br.com.nsfatima.gestao.organizacao.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PapelOrganizacionalTest {

    @Test
    @DisplayName("Should resolve all specific stored values")
    void shouldResolveAllStoredValuesIncludingAdmin() {
        assertThat(PapelOrganizacional.fromStoredValue("admin")).isEqualTo(PapelOrganizacional.ADMIN);
        assertThat(PapelOrganizacional.fromStoredValue("paroco")).isEqualTo(PapelOrganizacional.PAROCO);
        assertThat(PapelOrganizacional.fromStoredValue("vigario")).isEqualTo(PapelOrganizacional.VIGARIO);
        assertThat(PapelOrganizacional.fromStoredValue("coord-pastoral-global"))
                .isEqualTo(PapelOrganizacional.COORD_PASTORAL_GLOBAL);
        assertThat(PapelOrganizacional.fromStoredValue("coord-pastoral-local"))
                .isEqualTo(PapelOrganizacional.COORD_PASTORAL_LOCAL);
        assertThat(PapelOrganizacional.fromStoredValue("coord-movimento"))
                .isEqualTo(PapelOrganizacional.COORD_MOVIMENTO);
        assertThat(PapelOrganizacional.fromStoredValue("presidente-cpc"))
                .isEqualTo(PapelOrganizacional.PRESIDENTE_CPC);
        assertThat(PapelOrganizacional.fromStoredValue("membro-cpc"))
                .isEqualTo(PapelOrganizacional.MEMBRO_CPC);
        assertThat(PapelOrganizacional.fromStoredValue("membro-cpp"))
                .isEqualTo(PapelOrganizacional.MEMBRO_CPP);
        assertThat(PapelOrganizacional.fromStoredValue("mesa-cpp"))
                .isEqualTo(PapelOrganizacional.MESA_CPP);
        assertThat(PapelOrganizacional.fromStoredValue("voluntario-pastoral-local"))
                .isEqualTo(PapelOrganizacional.VOLUNTARIO_PASTORAL_LOCAL);
        assertThat(PapelOrganizacional.fromStoredValue("membro-movimento"))
                .isEqualTo(PapelOrganizacional.MEMBRO_MOVIMENTO);
        assertThat(PapelOrganizacional.fromStoredValue("padre")).isEqualTo(PapelOrganizacional.PADRE);
        assertThat(PapelOrganizacional.fromStoredValue("secretario")).isEqualTo(PapelOrganizacional.SECRETARIO);
        assertThat(PapelOrganizacional.COORD_PASTORAL_GLOBAL.getDescricao()).isEqualTo("Coordenador Pastoral Global");
    }

    @Test
    @DisplayName("Should return null for generic or unknown stored values")
    void shouldReturnNullForUnknownOrGenericStoredValues() {
        assertThat(PapelOrganizacional.fromStoredValue("coordenador")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue("vice-coordenador")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue("membro")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue("invalid")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue(null)).isNull();
    }
}
