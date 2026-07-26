package br.com.nsfatima.gestao.organizacao.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PapelOrganizacionalTest {

    @Test
    @DisplayName("Should resolve all stored values including ADMIN")
    void shouldResolveAllStoredValuesIncludingAdmin() {
        assertThat(PapelOrganizacional.fromStoredValue("admin")).isEqualTo(PapelOrganizacional.ADMIN);
        assertThat(PapelOrganizacional.fromStoredValue("paroco")).isEqualTo(PapelOrganizacional.PAROCO);
        assertThat(PapelOrganizacional.fromStoredValue("vigario")).isEqualTo(PapelOrganizacional.VIGARIO);
        assertThat(PapelOrganizacional.fromStoredValue("coordenador")).isEqualTo(PapelOrganizacional.COORDENADOR);
        assertThat(PapelOrganizacional.fromStoredValue("vice-coordenador")).isEqualTo(
                PapelOrganizacional.VICE_COORDENADOR);
        assertThat(PapelOrganizacional.fromStoredValue("padre")).isEqualTo(PapelOrganizacional.PADRE);
        assertThat(PapelOrganizacional.fromStoredValue("secretario")).isEqualTo(PapelOrganizacional.SECRETARIO);
        assertThat(PapelOrganizacional.fromStoredValue("membro")).isEqualTo(PapelOrganizacional.MEMBRO);
    }

    @Test
    @DisplayName("Should return null for unknown stored values")
    void shouldReturnNullForUnknownStoredValues() {
        assertThat(PapelOrganizacional.fromStoredValue("invalid")).isNull();
        assertThat(PapelOrganizacional.fromStoredValue(null)).isNull();
    }
}
