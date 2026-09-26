package br.com.nsfatima.gestao.patrimonio.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoGrupoParoquialInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Especifica a separação patrimonial entre unidades territoriais e grupos pastorais.
 */
@DisplayName("Policy: propriedade de unidade territorial")
class MatrizOuComunidadePropriedadePolicyTest {

    private final MatrizOuComunidadePropriedadePolicy policy = new MatrizOuComunidadePropriedadePolicy();

    /**
     * RN-PAT-01: matriz e comunidade são as únicas unidades territoriais aptas a possuir ativos.
     */
    @Test
    @DisplayName("permite matriz e comunidade como proprietárias")
    void permiteMatrizEComunidadeComoProprietarias() {
        assertThatCode(() -> policy.assertPermitida(TipoUnidadeTerritorial.MATRIZ)).doesNotThrowAnyException();
        assertThatCode(() -> policy.assertPermitida(TipoUnidadeTerritorial.COMUNIDADE)).doesNotThrowAnyException();
    }

    /**
     * RN-PAT-01: tipo territorial nulo ou inválido é rejeitado.
     */
    @Test
    @DisplayName("rejeita tipo territorial nulo")
    void rejeitaTipoTerritorialNulo() {
        assertThatThrownBy(() -> policy.assertPermitida(null))
                .isInstanceOf(AtivoGrupoParoquialInvalidaException.class);
    }
}
