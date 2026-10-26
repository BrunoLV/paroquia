package br.com.nsfatima.gestao.organizacao.domain.policy;

import br.com.nsfatima.gestao.organizacao.domain.model.FuncaoCoordenacao;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Papéis de vínculo por tipo de organização")
class PapeisPorTipoOrganizacaoTest {

    private static final Set<PapelOrganizacional> DE_GRUPO = EnumSet.of(PapelOrganizacional.COORDENADOR,
            PapelOrganizacional.SECRETARIO_GRUPO, PapelOrganizacional.MEMBRO_ATIVO, PapelOrganizacional.VOLUNTARIO);

    /** Matriz esperada, escrita à mão e independente do código de produção. */
    private static final Map<TipoOrganizacao, Set<PapelOrganizacional>> ESPERADO = Map.of(
            TipoOrganizacao.PASTORAL, DE_GRUPO,
            TipoOrganizacao.MOVIMENTO, DE_GRUPO,
            TipoOrganizacao.SERVICO_PAROQUIAL, DE_GRUPO,
            TipoOrganizacao.CONSELHO, EnumSet.of(PapelOrganizacional.SECRETARIO_CONSELHO,
                    PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL, PapelOrganizacional.MEMBRO_CONSELHO_ECONOMICO,
                    PapelOrganizacional.COORDENADOR, PapelOrganizacional.SECRETARIO_GRUPO));

    @ParameterizedTest
    @EnumSource(TipoOrganizacao.class)
    @DisplayName("aceita exatamente os papéis esperados, para todo tipo e todo papel")
    void segueAMatriz(TipoOrganizacao tipo) {
        for (PapelOrganizacional papel : PapelOrganizacional.values()) {
            assertThat(PapeisPorTipoOrganizacao.aceita(tipo, papel))
                    .as("%s em %s", papel, tipo).isEqualTo(ESPERADO.get(tipo).contains(papel));
        }
    }

    @ParameterizedTest
    @EnumSource(TipoOrganizacao.class)
    @DisplayName("o coordenador e o vice têm vínculo válido em todo tipo, pois continuam membros")
    void coordenacaoTemVinculoEmTodoTipo(TipoOrganizacao tipo) {
        for (FuncaoCoordenacao funcao : FuncaoCoordenacao.values()) {
            assertThat(PapeisPorTipoOrganizacao.aceita(tipo, funcao.papelEquivalente()))
                    .as("%s em %s", funcao, tipo).isTrue();
        }
    }

    @ParameterizedTest
    @EnumSource(TipoOrganizacao.class)
    @DisplayName("o papel de membro comum, para onde volta quem deixa a coordenação, é válido no tipo")
    void membroComumEValido(TipoOrganizacao tipo) {
        for (SubtipoConselho subtipo : SubtipoConselho.values()) {
            assertThat(PapeisPorTipoOrganizacao.aceita(tipo, PapeisPorTipoOrganizacao.papelDeMembro(tipo, subtipo)))
                    .as("%s/%s", tipo, subtipo).isTrue();
        }
    }

    @Test
    @DisplayName("o membro comum do conselho depende do subtipo; nos demais é membro ativo")
    void papelDeMembro() {
        assertThat(PapeisPorTipoOrganizacao.papelDeMembro(TipoOrganizacao.CONSELHO, SubtipoConselho.CAEP))
                .isEqualTo(PapelOrganizacional.MEMBRO_CONSELHO_ECONOMICO);
        assertThat(PapeisPorTipoOrganizacao.papelDeMembro(TipoOrganizacao.CONSELHO, SubtipoConselho.CPP))
                .isEqualTo(PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL);
        assertThat(PapeisPorTipoOrganizacao.papelDeMembro(TipoOrganizacao.PASTORAL, null))
                .isEqualTo(PapelOrganizacional.MEMBRO_ATIVO);
    }

    @Test
    @DisplayName("exigir recusa o papel que o tipo não admite")
    void exigirRecusa() {
        assertThatThrownBy(() -> PapeisPorTipoOrganizacao.exigir(TipoOrganizacao.PASTORAL,
                PapelOrganizacional.SECRETARIO_CONSELHO)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PASTORAL");
    }
}
