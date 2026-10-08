package br.com.nsfatima.gestao.membro.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class IdoneidadeAgenteTest {

    @Test
    void rejeitaDatasFuturasEValidadeAnteriorAoTermo() {
        LocalDate futuro = LocalDate.now().plusDays(1);
        assertThatThrownBy(() -> new IdoneidadeAgente(futuro, null, null, null, null, null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new IdoneidadeAgente(null, null, LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1), null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new IdoneidadeAgente(null, null, null, LocalDate.of(2030, 1, 1), null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void termoSemValidadeValeDesdeAAssinatura() {
        IdoneidadeAgente i = new IdoneidadeAgente(null, null, LocalDate.of(2020, 1, 1), null, null, null);
        assertThat(i.termoVigenteEm(LocalDate.of(2030, 1, 1))).isTrue();
        assertThat(IdoneidadeAgente.vazia().termoVigenteEm(LocalDate.of(2030, 1, 1))).isFalse();
    }
}
