package br.com.nsfatima.gestao.fiel.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VocacaoTest {

    @Test
    @DisplayName("Should resolve all stored values ignoring case and separators")
    void deveResolverTodosValoresArmazenados() {
        assertThat(Vocacao.fromStoredValue("LEIGO")).isEqualTo(Vocacao.LEIGO);
        assertThat(Vocacao.fromStoredValue("leigo")).isEqualTo(Vocacao.LEIGO);
        assertThat(Vocacao.fromStoredValue("SACERDOTE")).isEqualTo(Vocacao.SACERDOTE);
        assertThat(Vocacao.fromStoredValue("sacerdote")).isEqualTo(Vocacao.SACERDOTE);
        assertThat(Vocacao.fromStoredValue("DIACONO")).isEqualTo(Vocacao.DIACONO);
        assertThat(Vocacao.fromStoredValue("diacono")).isEqualTo(Vocacao.DIACONO);
        assertThat(Vocacao.fromStoredValue("RELIGIOSO")).isEqualTo(Vocacao.RELIGIOSO);
        assertThat(Vocacao.fromStoredValue("religioso")).isEqualTo(Vocacao.RELIGIOSO);
    }

    @Test
    @DisplayName("Should normalize surrounding whitespace and underscore/hyphen separators")
    void deveNormalizarEspacosESeparadores() {
        assertThat(Vocacao.fromStoredValue("  sacerdote  ")).isEqualTo(Vocacao.SACERDOTE);
        assertThat(Vocacao.fromStoredValue("Sacerdote")).isEqualTo(Vocacao.SACERDOTE);
    }

    @Test
    @DisplayName("Should return null for blank, invalid or null stored values")
    void deveRetornarNuloParaValorInvalidoOuNulo() {
        assertThat(Vocacao.fromStoredValue(null)).isNull();
        assertThat(Vocacao.fromStoredValue("")).isNull();
        assertThat(Vocacao.fromStoredValue("   ")).isNull();
        assertThat(Vocacao.fromStoredValue("invalido")).isNull();
        assertThat(Vocacao.fromStoredValue("PAROCO")).isNull();
    }

    @Test
    @DisplayName("Should expose a stable stored value independent of enum constant name")
    void deveExporValorArmazenadoEstavel() {
        assertThat(Vocacao.LEIGO.storedValue()).isEqualTo("leigo");
        assertThat(Vocacao.SACERDOTE.storedValue()).isEqualTo("sacerdote");
        assertThat(Vocacao.DIACONO.storedValue()).isEqualTo("diacono");
        assertThat(Vocacao.RELIGIOSO.storedValue()).isEqualTo("religioso");
    }

    @Test
    @DisplayName("fromJson should behave like fromStoredValue for @JsonCreator deserialization")
    void fromJsonDeveDelegarParaFromStoredValue() {
        assertThat(Vocacao.fromJson("sacerdote")).isEqualTo(Vocacao.SACERDOTE);
        assertThat(Vocacao.fromJson("invalido")).isNull();
    }
}
