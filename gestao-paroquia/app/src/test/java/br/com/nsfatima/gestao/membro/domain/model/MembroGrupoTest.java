package br.com.nsfatima.gestao.membro.domain.model;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MembroGrupoTest {

    private MembroGrupo novo(PapelOrganizacional papel) {
        return MembroGrupo.novo(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TipoGrupo.PASTORAL, papel, null, LocalDate.of(2026, 1, 1));
    }

    @Test
    void novoNasceAtivo() {
        assertThat(novo(PapelOrganizacional.MEMBRO_PASTORAL).isAtivo()).isTrue();
    }

    @Test
    void rejeitaPapelNulo() {
        assertThatThrownBy(() -> novo(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void rejeitaPapelGlobal() {
        assertThatThrownBy(() -> novo(PapelOrganizacional.PAROCO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("global");
    }

    @Test
    void atribuirPapelCoordenacaoMarcaExercicioDeCoordenacao() {
        MembroGrupo m = novo(PapelOrganizacional.MEMBRO_PASTORAL);
        assertThat(m.exercerCoordenacao()).isFalse();
        m.atribuirPapel(PapelOrganizacional.COORD_PASTORAL_LOCAL);
        assertThat(m.exercerCoordenacao()).isTrue();
    }

    @Test
    void ativarDesativar() {
        MembroGrupo m = novo(PapelOrganizacional.MEMBRO_PASTORAL);
        m.desativar();
        assertThat(m.isAtivo()).isFalse();
        m.ativar();
        assertThat(m.isAtivo()).isTrue();
    }

    @Test
    void isCoordenacaoConsolidada() {
        assertThat(PapelOrganizacional.MESA_CPP.isCoordenacao()).isTrue();
        assertThat(PapelOrganizacional.PRESIDENTE_CPC.isCoordenacao()).isTrue();
        assertThat(PapelOrganizacional.MEMBRO_CPC.isCoordenacao()).isFalse();
        assertThat(PapelOrganizacional.ADMIN.isGlobal()).isTrue();
        assertThat(PapelOrganizacional.MESA_CPP.isGlobal()).isFalse();
    }
}
