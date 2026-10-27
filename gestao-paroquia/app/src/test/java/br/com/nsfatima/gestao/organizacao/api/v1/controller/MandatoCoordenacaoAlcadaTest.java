package br.com.nsfatima.gestao.organizacao.api.v1.controller;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RN-ORG-05: o mandato é proposto por Pároco, Bispo ou mesa do CPP, mas homologar, vetar e encerrar é
 * competência exclusiva do Pároco.
 */
class MandatoCoordenacaoAlcadaTest {

    private static String alcada(String metodo) {
        Method alvo = Arrays.stream(MandatoCoordenacaoController.class.getDeclaredMethods())
                .filter(m -> m.getName().equals(metodo))
                .findFirst().orElseThrow();
        return alvo.getAnnotation(PreAuthorize.class).value();
    }

    @Test
    void homologarVetarEEncerrarSaoExclusivosDoParoco() {
        for (String metodo : List.of("homologar", "vetar", "encerrar")) {
            assertThat(alcada(metodo)).contains("PAROCO").doesNotContain("BISPO");
        }
    }

    @Test
    void propostaAdmiteBispo() {
        assertThat(alcada("propor")).contains("BISPO_DIOCESANO");
    }
}
