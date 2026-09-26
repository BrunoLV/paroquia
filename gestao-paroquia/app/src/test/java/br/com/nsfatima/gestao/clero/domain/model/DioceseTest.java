package br.com.nsfatima.gestao.clero.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Testes de Domínio: Diocese")
class DioceseTest {

    @Test
    @DisplayName("Deve instanciar Diocese válida com sucesso")
    void deveInstanciarDioceseValida() {
        UUID id = UUID.randomUUID();
        Diocese diocese = new Diocese(
                id,
                "Diocese de Santos",
                "Dom Tarcísio Scaramussa",
                "12.345.678/0001-90",
                "Santos",
                "SP",
                "contato@diocesedesantos.org.br",
                true
        );

        assertThat(diocese.getId()).isEqualTo(id);
        assertThat(diocese.getNome()).isEqualTo("Diocese de Santos");
        assertThat(diocese.getBispoDiocesano()).isEqualTo("Dom Tarcísio Scaramussa");
        assertThat(diocese.getCnpjMitra()).isEqualTo("12.345.678/0001-90");
        assertThat(diocese.getCidade()).isEqualTo("Santos");
        assertThat(diocese.getUf()).isEqualTo("SP");
        assertThat(diocese.getContato()).isEqualTo("contato@diocesedesantos.org.br");
        assertThat(diocese.isAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve rejeitar Diocese com nome nulo ou em branco")
    void deveRejeitarNomeInvalido() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> new Diocese(id, null, "Bispo", null, null, null, null, true))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Nome da diocese é obrigatório");

        assertThatThrownBy(() -> new Diocese(id, "   ", "Bispo", null, null, null, null, true))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Nome da diocese não pode estar em branco");
    }

    @Test
    @DisplayName("Deve atualizar dados da Diocese com sucesso")
    void deveAtualizarDiocese() {
        Diocese diocese = new Diocese(
                UUID.randomUUID(),
                "Diocese Antiga",
                "Bispo Antigo",
                null,
                null,
                null,
                null,
                true
        );

        diocese.atualizar("Diocese Nova", "Bispo Novo", "11.222.333/0001-44", "Capital", "SP", "novo@test.com", false);

        assertThat(diocese.getNome()).isEqualTo("Diocese Nova");
        assertThat(diocese.getBispoDiocesano()).isEqualTo("Bispo Novo");
        assertThat(diocese.getCnpjMitra()).isEqualTo("11.222.333/0001-44");
        assertThat(diocese.getCidade()).isEqualTo("Capital");
        assertThat(diocese.getUf()).isEqualTo("SP");
        assertThat(diocese.getContato()).isEqualTo("novo@test.com");
        assertThat(diocese.isAtivo()).isFalse();
    }
}
