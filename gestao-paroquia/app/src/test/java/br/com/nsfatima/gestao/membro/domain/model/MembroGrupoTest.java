package br.com.nsfatima.gestao.membro.domain.model;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MembroGrupoTest {

    private MembroGrupo novo(PapelOrganizacional papel) {
        return MembroGrupo.novo(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(),
                TipoOrganizacao.PASTORAL, papel, null, LocalDate.of(2026, 1, 1));
    }

    @Test
    void novoNasceAtivo() {
        assertThat(novo(PapelOrganizacional.MEMBRO_ATIVO).isAtivo()).isTrue();
    }

    @Test
    void rejeitaPapelNulo() {
        assertThatThrownBy(() -> novo(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void atribuirPapelCoordenacaoMarcaExercicioDeCoordenacao() {
        MembroGrupo m = novo(PapelOrganizacional.MEMBRO_ATIVO);
        assertThat(m.exercerCoordenacao()).isFalse();
        m.atribuirPapel(PapelOrganizacional.COORDENADOR);
        assertThat(m.exercerCoordenacao()).isTrue();
    }

    @Test
    void ativarDesativar() {
        MembroGrupo m = novo(PapelOrganizacional.MEMBRO_ATIVO);
        m.desativar();
        assertThat(m.isAtivo()).isFalse();
        m.ativar();
        assertThat(m.isAtivo()).isTrue();
    }

    @Test
    void isCoordenacaoConsolidada() {
        assertThat(PapelOrganizacional.COORDENADOR.isCoordenacao()).isTrue();
        assertThat(PapelOrganizacional.SECRETARIO_CONSELHO.isCoordenacao()).isFalse();
        assertThat(PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL.isCoordenacao()).isFalse();
        assertThat(PapelOrganizacional.MEMBRO_ATIVO.isCoordenacao()).isFalse();
    }
}
