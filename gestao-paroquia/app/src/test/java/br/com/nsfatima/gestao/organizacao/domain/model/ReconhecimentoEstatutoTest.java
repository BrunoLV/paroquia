package br.com.nsfatima.gestao.organizacao.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReconhecimentoEstatutoTest {

    @Test
    void exigeAutoridadeEDataNaoFutura() {
        assertThatThrownBy(() -> new ReconhecimentoEstatuto(" ", LocalDate.of(2020, 1, 1), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReconhecimentoEstatuto("Bispo", LocalDate.now().plusDays(1), null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new ReconhecimentoEstatuto("Bispo", null, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void normalizaCamposTextuais() {
        ReconhecimentoEstatuto r = new ReconhecimentoEstatuto(" Bispo ", LocalDate.of(2020, 1, 1), " ");
        assertThat(r.autoridade()).isEqualTo("Bispo");
        assertThat(r.referencia()).isNull();
    }

    @Test
    void aliasLegadoMinisterioViraServicoParoquial() {
        assertThat(TipoOrganizacao.fromJson("MINISTERIO")).isEqualTo(TipoOrganizacao.SERVICO_PAROQUIAL);
        assertThat(TipoOrganizacao.fromJson("outro")).isEqualTo(TipoOrganizacao.SERVICO_PAROQUIAL);
        assertThat(SubtipoConselho.CAEP.isPastoral()).isFalse();
        assertThat(SubtipoConselho.CPC.isPastoral()).isTrue();
    }
}
