package br.com.nsfatima.gestao.calendario.domain.type;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CategoriaEventoTest {

    @Test
    @DisplayName("Deve conter todos os valores esperados")
    void deveConterTodosOsValores() {
        assertDoesNotThrow(() -> CategoriaEvento.valueOf("PASTORAL"));
        assertDoesNotThrow(() -> CategoriaEvento.valueOf("SOCIAL"));
        assertDoesNotThrow(() -> CategoriaEvento.valueOf("LITURGICO"));
        assertDoesNotThrow(() -> CategoriaEvento.valueOf("ADMINISTRATIVO"));
        assertDoesNotThrow(() -> CategoriaEvento.valueOf("SACRAMENTAL"));
        assertDoesNotThrow(() -> CategoriaEvento.valueOf("FORMATIVO"));
        assertDoesNotThrow(() -> CategoriaEvento.valueOf("ASSISTENCIAL"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"PASTORAL", "pastoral", "  Social  ", "ADMINISTRATIVO"})
    @DisplayName("Deve converter de string ignorando case e espaços")
    void deveConverterDeString(String value) {
        CategoriaEvento result = CategoriaEvento.fromValue(value);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Deve lançar exceção para valor inválido")
    void deveLancarexcecaoParaValorInvalido() {
        assertThrows(IllegalArgumentException.class, () -> CategoriaEvento.fromValue("INVALIDO"));
    }
}
