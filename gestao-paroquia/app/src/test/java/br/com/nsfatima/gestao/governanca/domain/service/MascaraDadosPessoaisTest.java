package br.com.nsfatima.gestao.governanca.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Mascaramento de dados pessoais em respostas publicas")
class MascaraDadosPessoaisTest {

    @Test
    @DisplayName("CPF mantem somente os dois ultimos digitos")
    void cpf() {
        assertThat(MascaraDadosPessoais.cpf("529.982.247-25")).isEqualTo("***.***.***-25");
        assertThat(MascaraDadosPessoais.cpf(" ")).isNull();
        assertThat(MascaraDadosPessoais.cpf(null)).isNull();
    }

    @Test
    @DisplayName("E-mail mantem extremos do usuario e o dominio; entrada invalida vira placeholder")
    void email() {
        assertThat(MascaraDadosPessoais.email("maria@exemplo.com")).isEqualTo("m***a@exemplo.com");
        assertThat(MascaraDadosPessoais.email("ab@exemplo.com")).isEqualTo("a***@exemplo.com");
        assertThat(MascaraDadosPessoais.email("sem-arroba")).isEqualTo("***@***");
        assertThat(MascaraDadosPessoais.email(null)).isEqualTo("***@***");
        assertThat(MascaraDadosPessoais.email("@dominio.com")).isEqualTo("***@***");
    }

    @Test
    @DisplayName("Nome mantem apenas a inicial de cada palavra")
    void nome() {
        assertThat(MascaraDadosPessoais.nome("Maria  da Silva")).isEqualTo("M*** d*** S***");
        assertThat(MascaraDadosPessoais.nome("")).isNull();
    }
}
