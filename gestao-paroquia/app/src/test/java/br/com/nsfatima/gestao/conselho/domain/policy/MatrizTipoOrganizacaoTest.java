package br.com.nsfatima.gestao.conselho.domain.policy;

import br.com.nsfatima.gestao.conselho.MatrizCompatibilidade;
import br.com.nsfatima.gestao.conselho.MatrizCompatibilidade.Orgao;
import br.com.nsfatima.gestao.conselho.domain.exception.TipoReuniaoIncompativelException;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ReferenciaOrganizacao;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Matriz tipo de reunião × tipo de organização")
class MatrizTipoOrganizacaoTest {

    private static final UUID MATRIZ = UUID.randomUUID();
    private static final UUID COMUNIDADE = UUID.randomUUID();

    static Stream<Object[]> todasAsCombinacoes() {
        return Stream.of(TipoReuniao.values()).flatMap(tipo -> MatrizCompatibilidade.TODOS.stream()
                .map(orgao -> new Object[] {tipo, orgao}));
    }

    private static ReferenciaOrganizacao ref(Orgao orgao, UUID unidade) {
        return new ReferenciaOrganizacao(UUID.randomUUID(), orgao.tipo(), orgao.subtipo(), unidade);
    }

    @ParameterizedTest(name = "{0} convocada por {1}")
    @MethodSource("todasAsCombinacoes")
    @DisplayName("a política aceita exatamente os órgãos da matriz esperada, para todo tipo e todo órgão")
    void politicaSegueAMatriz(TipoReuniao tipo, Orgao orgao) {
        assertThat(RegrasTipoReuniaoPolicy.para(tipo).aceitaConvocante(ref(orgao, MATRIZ)))
                .isEqualTo(MatrizCompatibilidade.pode(tipo, orgao));
    }

    @ParameterizedTest(name = "{0} convocada por {1}")
    @MethodSource("todasAsCombinacoes")
    @DisplayName("a criação da ata recusa o órgão fora da matriz")
    void criacaoSegueAMatriz(TipoReuniao tipo, Orgao orgao) {
        ReferenciaOrganizacao convocante = ref(orgao, MATRIZ);
        ReferenciaOrganizacao associada = RegrasTipoReuniaoPolicy.para(tipo).exigeOrganizacaoAssociada()
                ? ref(new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CAEP), MATRIZ) : null;

        var criacao = assertThatCode(() -> AtaReuniao.exigirOrganizacoesCompativeis(tipo, convocante, associada));

        if (MatrizCompatibilidade.pode(tipo, orgao)) {
            criacao.doesNotThrowAnyException();
        } else {
            criacao.isInstanceOf(TipoReuniaoIncompativelException.class);
        }
    }

    @Test
    @DisplayName("a reunião conjunta exige o CAEP da mesma unidade do CPP")
    void conjuntaExigeMesmaUnidade() {
        ReferenciaOrganizacao cpp = ref(new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CPP), MATRIZ);
        ReferenciaOrganizacao caepDaMatriz = ref(new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CAEP), MATRIZ);
        ReferenciaOrganizacao caepDaComunidade =
                ref(new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CAEP), COMUNIDADE);
        ReferenciaOrganizacao cpcDaMatriz = ref(new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CPC), MATRIZ);
        TipoReuniao conjunta = TipoReuniao.REUNIAO_CONJUNTA_CPP_CAEP;

        assertThatCode(() -> AtaReuniao.exigirOrganizacoesCompativeis(conjunta, cpp, caepDaMatriz))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> AtaReuniao.exigirOrganizacoesCompativeis(conjunta, cpp, caepDaComunidade))
                .isInstanceOf(TipoReuniaoIncompativelException.class).hasMessageContaining("mesma unidade");
        assertThatThrownBy(() -> AtaReuniao.exigirOrganizacoesCompativeis(conjunta, cpp, cpcDaMatriz))
                .isInstanceOf(TipoReuniaoIncompativelException.class);
        assertThatThrownBy(() -> AtaReuniao.exigirOrganizacoesCompativeis(conjunta, cpp, null))
                .isInstanceOf(TipoReuniaoIncompativelException.class);
    }

    @Test
    @DisplayName("tipo que não é conjunta não admite organização associada")
    void associadaSoNaConjunta() {
        ReferenciaOrganizacao cpp = ref(new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CPP), MATRIZ);
        ReferenciaOrganizacao caep = ref(new Orgao(TipoOrganizacao.CONSELHO, SubtipoConselho.CAEP), MATRIZ);

        assertThatThrownBy(() -> AtaReuniao.exigirOrganizacoesCompativeis(TipoReuniao.REUNIAO_CPP, cpp, caep))
                .isInstanceOf(TipoReuniaoIncompativelException.class);
    }
}
