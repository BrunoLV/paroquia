package br.com.nsfatima.gestao.fiel.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FielTest {

    @Test
    void deveExporTodosOsComponentesDoRecord() {
        UUID id = UUID.randomUUID();
        LocalDate dataNascimento = LocalDate.of(1978, 7, 22);

        Endereco endereco = new Endereco("01000-000", "Rua Nova", "10", "Apto 1", "Centro", "São Paulo", "SP");
        VidaSacramental vidaSacramental = new VidaSacramental(
                true, LocalDate.of(1978, 8, 1), "Matriz", "Padre João", "01", "10", "100", "Maria e José",
                true, LocalDate.of(1987, 5, 10), "Matriz", "Turma 1987",
                false, null, null, null, null,
                false, null, null, null, null
        );
        ConsentimentoLgpd consentimento = new ConsentimentoLgpd(true, Instant.now(), "127.0.0.1", "v1.0", true, true);
        Dependente dependente = new Dependente(
                UUID.randomUUID(), "Filho 1", LocalDate.of(2010, 1, 1), Parentesco.FILHO, true, false, false);

        Fiel fiel = new Fiel(
                id, "Ana Costa", "12345678900", "12.345.678-9", dataNascimento,
                Sexo.FEMININO, "Engenheira", "ana@example.com", "11977776666",
                "1133334444", "11977776666", PreferenciaCanalComunicacao.WHATSAPP,
                endereco, EstadoCivil.SOLTEIRO, null, Vocacao.LEIGO, true,
                vidaSacramental, List.of(dependente), "Som e Mídia", consentimento
        );

        assertThat(fiel.id()).isEqualTo(id);
        assertThat(fiel.nome()).isEqualTo("Ana Costa");
        assertThat(fiel.cpf()).isEqualTo("12345678900");
        assertThat(fiel.rg()).isEqualTo("12.345.678-9");
        assertThat(fiel.dataNascimento()).isEqualTo(dataNascimento);
        assertThat(fiel.sexo()).isEqualTo(Sexo.FEMININO);
        assertThat(fiel.email()).isEqualTo("ana@example.com");
        assertThat(fiel.telefone()).isEqualTo("11977776666");
        assertThat(fiel.endereco().logradouro()).isEqualTo("Rua Nova");
        assertThat(fiel.vocacao()).isEqualTo(Vocacao.LEIGO);
        assertThat(fiel.ativo()).isTrue();
        assertThat(fiel.batizado()).isTrue();
        assertThat(fiel.primeiraComunhao()).isTrue();
        assertThat(fiel.crismado()).isFalse();
        assertThat(fiel.casadoIgrejaCatolica()).isFalse();
        assertThat(fiel.dependentes()).hasSize(1);
    }
}
