package br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FielEntityTest {

    @Test
    void deveExporTodosOsCamposViaGettersESetters() {
        FielEntity fiel = new FielEntity();
        UUID id = UUID.randomUUID();
        Instant criadoEm = Instant.now();
        Instant atualizadoEm = criadoEm.plusSeconds(30);

        fiel.setId(id);
        fiel.setNome("José Pereira");
        fiel.setCpf("98765432100");
        fiel.setDataNascimento(LocalDate.of(1985, 3, 10));
        fiel.setEmail("jose@example.com");
        fiel.setTelefone("11988887777");
        fiel.setEndereco("Av. Principal, 456");
        fiel.setVocacao(Vocacao.SACERDOTE);
        fiel.setAtivo(false);
        fiel.setCriadoEmUtc(criadoEm);
        fiel.setAtualizadoEmUtc(atualizadoEm);
        fiel.setCriadoPor("secretaria");
        fiel.setAtualizadoPor("paroco");

        assertThat(fiel.getId()).isEqualTo(id);
        assertThat(fiel.getNome()).isEqualTo("José Pereira");
        assertThat(fiel.getCpf()).isEqualTo("98765432100");
        assertThat(fiel.getDataNascimento()).isEqualTo(LocalDate.of(1985, 3, 10));
        assertThat(fiel.getEmail()).isEqualTo("jose@example.com");
        assertThat(fiel.getTelefone()).isEqualTo("11988887777");
        assertThat(fiel.getEndereco()).isEqualTo("Av. Principal, 456");
        assertThat(fiel.getVocacao()).isEqualTo(Vocacao.SACERDOTE);
        assertThat(fiel.isAtivo()).isFalse();
        assertThat(fiel.getCriadoEmUtc()).isEqualTo(criadoEm);
        assertThat(fiel.getAtualizadoEmUtc()).isEqualTo(atualizadoEm);
        assertThat(fiel.getCriadoPor()).isEqualTo("secretaria");
        assertThat(fiel.getAtualizadoPor()).isEqualTo("paroco");
    }

    @Test
    void deveCalcularCpfHashAoDefinirCpf() {
        FielEntity fiel = new FielEntity();

        fiel.setCpf("11122233344");

        assertThat(fiel.getCpfHash())
                .isNotNull()
                .hasSize(64)
                .isNotEqualTo("11122233344");
    }

    @Test
    void mesmoCpfDeveGerarOMesmoHashEmInstanciasDiferentes() {
        FielEntity primeiro = new FielEntity();
        FielEntity segundo = new FielEntity();

        primeiro.setCpf("55566677788");
        segundo.setCpf("55566677788");

        assertThat(primeiro.getCpfHash()).isEqualTo(segundo.getCpfHash());
    }
}
