package br.com.nsfatima.gestao.fiel.domain.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidacaoDocumentoCivilTest {

    private final ValidacaoDocumentoCivil validacao = new ValidacaoDocumentoCivil();

    @Test
    void paisPrecisaSerIso3() {
        assertThatCode(() -> validacao.validarPais("BRA")).doesNotThrowAnyException();
        assertThatThrownBy(() -> validacao.validarPais("BR")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> validacao.validarPais(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void brasileiroExigeCpfValido() {
        assertThatCode(() -> validacao.validarDocumento("BRA", "529.982.247-25", null, null))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> validacao.validarDocumento("BRA", "111.111.111-11", null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> validacao.validarDocumento("BRA", null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void estrangeiroExigeDocumentoValido() {
        assertThatCode(() -> validacao.validarDocumento("USA", null, "PASSAPORTE", "X123"))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> validacao.validarDocumento("USA", null, "RG", "X123"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> validacao.validarDocumento("USA", null, "PASSAPORTE", "  "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
