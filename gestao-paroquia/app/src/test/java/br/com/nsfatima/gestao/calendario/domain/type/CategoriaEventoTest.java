package br.com.nsfatima.gestao.calendario.domain.type;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    @DisplayName("Deve retornar o nível de prioridade correto para cada categoria de evento")
    void deveRetornarNivelPrioridadeCorreto() {
        assertEquals(1, CategoriaEvento.SOLENIDADE.getNivelPrioridade());
        assertEquals(2, CategoriaEvento.LITURGICO.getNivelPrioridade());
        assertEquals(2, CategoriaEvento.SACRAMENTAL.getNivelPrioridade());
        assertEquals(3, CategoriaEvento.PASTORAL.getNivelPrioridade());
        assertEquals(3, CategoriaEvento.FORMATIVO.getNivelPrioridade());
        assertEquals(3, CategoriaEvento.ADMINISTRATIVO.getNivelPrioridade());
        assertEquals(3, CategoriaEvento.ASSISTENCIAL.getNivelPrioridade());
        assertEquals(3, CategoriaEvento.SOCIAL.getNivelPrioridade());
        assertEquals(4, CategoriaEvento.MOVIMENTO.getNivelPrioridade());
    }
}
