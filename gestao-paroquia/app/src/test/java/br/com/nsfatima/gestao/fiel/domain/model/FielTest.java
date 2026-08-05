package br.com.nsfatima.gestao.fiel.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FielTest {

    @Test
    void deveExporTodosOsComponentesDoRecord() {
        UUID id = UUID.randomUUID();
        LocalDate dataNascimento = LocalDate.of(1978, 7, 22);

        Fiel fiel = new Fiel(
                id, "Ana Costa", "12345678900", dataNascimento,
                "ana@example.com", "11977776666", "Rua Nova, 10", Vocacao.LEIGO, true,
                true, true, false, false);

        assertThat(fiel.id()).isEqualTo(id);
        assertThat(fiel.nome()).isEqualTo("Ana Costa");
        assertThat(fiel.cpf()).isEqualTo("12345678900");
        assertThat(fiel.dataNascimento()).isEqualTo(dataNascimento);
        assertThat(fiel.email()).isEqualTo("ana@example.com");
        assertThat(fiel.telefone()).isEqualTo("11977776666");
        assertThat(fiel.endereco()).isEqualTo("Rua Nova, 10");
        assertThat(fiel.vocacao()).isEqualTo(Vocacao.LEIGO);
        assertThat(fiel.ativo()).isTrue();
        assertThat(fiel.batizado()).isTrue();
        assertThat(fiel.primeiraComunhao()).isTrue();
        assertThat(fiel.crismado()).isFalse();
        assertThat(fiel.casadoIgrejaCatolica()).isFalse();
    }

    @Test
    void doisFieisComOsMesmosValoresDevemSerIguais() {
        UUID id = UUID.randomUUID();
        LocalDate dataNascimento = LocalDate.of(1978, 7, 22);

        Fiel a = new Fiel(id, "Ana Costa", "12345678900", dataNascimento,
                "ana@example.com", "11977776666", "Rua Nova, 10", Vocacao.LEIGO, true,
                true, true, false, false);
        Fiel b = new Fiel(id, "Ana Costa", "12345678900", dataNascimento,
                "ana@example.com", "11977776666", "Rua Nova, 10", Vocacao.LEIGO, true,
                true, true, false, false);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.toString()).contains("Ana Costa");
    }
}
