package br.com.nsfatima.gestao.clero.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class GrauOrdemTest {

    @Test
    void deveExigirOrdenacoesCompativeisComBispo() {
        assertThrows(IllegalArgumentException.class,
                () -> GrauOrdem.BISPO.validarOrdenacoes(LocalDate.of(1990, 1, 1), null, null));
    }

    @Test
    void deveAceitarOrdenacoesCompletasDeBispo() {
        assertDoesNotThrow(() -> GrauOrdem.BISPO.validarOrdenacoes(
                LocalDate.of(1990, 1, 1), LocalDate.of(1991, 1, 1), LocalDate.of(2000, 1, 1)));
    }
}
