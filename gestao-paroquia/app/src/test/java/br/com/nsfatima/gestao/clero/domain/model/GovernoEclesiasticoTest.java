package br.com.nsfatima.gestao.clero.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Domínio: GovernoEclesiastico")
class GovernoEclesiasticoTest {

    @Test
    @DisplayName("Deve instanciar Governo Eclesiástico válido com sucesso")
    void deveInstanciarValido() {
        UUID id = UUID.randomUUID();
        UUID dioceseId = UUID.randomUUID();
        UUID parocoId = UUID.randomUUID();
        LocalDate posse = LocalDate.of(2020, 2, 1);
        LocalDate instalacao = LocalDate.of(1950, 10, 12);

        GovernoEclesiastico governo = new GovernoEclesiastico(
                id,
                dioceseId,
                parocoId,
                posse,
                "Decreto 12/1950",
                instalacao,
                "01/1950"
        );

        assertThat(governo.getId()).isEqualTo(id);
        assertThat(governo.getDioceseId()).isEqualTo(dioceseId);
        assertThat(governo.getParocoId()).isEqualTo(parocoId);
        assertThat(governo.getDataPosseParoco()).isEqualTo(posse);
        assertThat(governo.getDecretoErecaoParoquia()).isEqualTo("Decreto 12/1950");
        assertThat(governo.getDataInstalacaoParoquia()).isEqualTo(instalacao);
        assertThat(governo.getLivroTomboNumero()).isEqualTo("01/1950");
    }

    @Test
    @DisplayName("Deve rejeitar Governo Eclesiástico sem diocese vinculada")
    void deveRejeitarSemDiocese() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> new GovernoEclesiastico(id, null, null, null, null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Diocese vinculada é obrigatória");
    }
}
