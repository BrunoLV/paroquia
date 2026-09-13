package br.com.nsfatima.gestao.iam.domain.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PapeisTest {

    @Test
    void deCsvToleraNuloEBrancos() {
        assertThat(Papeis.deCsv(null).paraCsv()).isNull();
        assertThat(Papeis.deCsv("  ").paraCsv()).isNull();
        assertThat(Papeis.deCsv("ROLE_ADMIN, ROLE_USER ,").valores())
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void adicionarEIdempotente() {
        Papeis p = Papeis.deCsv("ROLE_ADMIN").adicionar("ROLE_USER").adicionar("ROLE_USER");
        assertThat(p.paraCsv()).isEqualTo("ROLE_ADMIN,ROLE_USER");
    }

    @Test
    void adicionarACsvVazioProduzApenasOPapel() {
        assertThat(Papeis.deCsv(null).adicionar("ROLE_USER").paraCsv()).isEqualTo("ROLE_USER");
    }

    @Test
    void removerEContem() {
        Papeis p = Papeis.deCsv("ROLE_ADMIN,ROLE_USER");
        assertThat(p.contem("ROLE_ADMIN")).isTrue();
        assertThat(p.remover("ROLE_ADMIN").contem("ROLE_ADMIN")).isFalse();
    }
}
