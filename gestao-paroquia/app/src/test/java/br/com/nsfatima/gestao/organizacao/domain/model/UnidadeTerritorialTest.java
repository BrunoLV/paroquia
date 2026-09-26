package br.com.nsfatima.gestao.organizacao.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Domínio: UnidadeTerritorial")
class UnidadeTerritorialTest {

    @Test
    @DisplayName("Igreja Matriz deve possuir Sacrário e Pia Batismal obrigatoriamente por regra canônica")
    void matrizDevePossuirSacrarioEPiaBatismal() {
        UUID id = UUID.randomUUID();
        UnidadeTerritorial matriz = new UnidadeTerritorial(
                id,
                "Igreja Matriz Nossa Senhora de Fátima",
                TipoUnidadeTerritorial.MATRIZ,
                true,
                null,
                "Nossa Senhora de Fátima",
                "05-13",
                LocalDate.of(1960, 5, 13),
                false, // tentativa de setar falso
                false, // tentativa de setar falso
                null,
                "12.345.678/0001-90",
                null, null, null, null, null, null,
                BigDecimal.valueOf(1000), BigDecimal.valueOf(800),
                "AVCB-1234", LocalDate.of(2027, 12, 31), 450,
                "EN-987", "AG-654",
                "11988887777", "matriz@nsfatima.org.br",
                new EnderecoUnidade("08000-000", "Rua das Flores", "100", null, "Centro", "São Paulo", "SP")
        );

        assertThat(matriz.isPossuiSacrario()).isTrue();
        assertThat(matriz.isPossuiPiaBatismal()).isTrue();
        assertThat(matriz.getTipo()).isEqualTo(TipoUnidadeTerritorial.MATRIZ);
        assertThat(matriz.getPadroeiro()).isEqualTo("Nossa Senhora de Fátima");
        assertThat(matriz.getDataFestaPadroeira()).isEqualTo("05-13");
        assertThat(matriz.getAreaTerrenoM2()).isEqualTo(BigDecimal.valueOf(1000));
        assertThat(matriz.getEndereco().logradouro()).isEqualTo("Rua das Flores");
    }

    @Test
    @DisplayName("Capela/Comunidade pode não ter Pia Batismal")
    void comunidadePodeNaoTerPiaBatismal() {
        UUID matrizId = UUID.randomUUID();
        UUID capelaId = UUID.randomUUID();

        UnidadeTerritorial capela = new UnidadeTerritorial(
                capelaId,
                "Capela São José",
                TipoUnidadeTerritorial.COMUNIDADE,
                true,
                matrizId,
                "São José",
                "03-19",
                null,
                true,
                false, // Comunidade sem pia batismal
                null,
                null, null, null, null, null, null, null,
                null, null,
                null, null, null,
                null, null,
                null, null,
                null
        );

        assertThat(capela.isPossuiSacrario()).isTrue();
        assertThat(capela.isPossuiPiaBatismal()).isFalse();
        assertThat(capela.getParentId()).isEqualTo(matrizId);
    }

    @Test
    @DisplayName("Não deve permitir que uma unidade territorial seja mãe de si mesma")
    void naoDevePermitirAutoReferencia() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> new UnidadeTerritorial(
                id,
                "Unidade Auto-referenciada",
                TipoUnidadeTerritorial.COMUNIDADE,
                true,
                id, // parentId = id
                null, null, null, true, false, null,
                null, null, null, null, null, null, null,
                null, null, null, null, null, null, null,
                null, null, null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Uma unidade territorial não pode ser mãe de si mesma");
    }
}
