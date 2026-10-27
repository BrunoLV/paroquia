package br.com.nsfatima.gestao.fiel.domain.policy;

import br.com.nsfatima.gestao.fiel.domain.policy.PoliticaVisaoFiel.Visao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class PoliticaVisaoFielTest {

    @ParameterizedTest
    @CsvSource({
            "admin,COMPLETA", "paroco,COMPLETA", "bispo-diocesano,COMPLETA", "secretaria-paroquial,COMPLETA",
            "ROLE_PAROCO,COMPLETA", "coordenador,CONTATO", "secretario-conselho,NOME",
            "membro-conselho-pastoral,NOME", "vigario,NOME", "desconhecido,NOME"})
    void cadaPapelRecebeSuaVisao(String papel, Visao esperada) {
        assertThat(PoliticaVisaoFiel.para(papel)).isEqualTo(esperada);
    }

    @Test
    void papelNuloRecebeAVisaoMaisRestrita() {
        assertThat(PoliticaVisaoFiel.para(null)).isEqualTo(Visao.NOME);
    }
}
