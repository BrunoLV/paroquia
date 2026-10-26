package br.com.nsfatima.gestao.conselho.domain.policy;

import br.com.nsfatima.gestao.conselho.domain.model.ModoPresenca;
import br.com.nsfatima.gestao.conselho.domain.model.ReferenciaOrganizacao;
import br.com.nsfatima.gestao.conselho.domain.model.ResultadoQuorum;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RegrasTipoReuniaoPolicyTest {

    private static ReferenciaOrganizacao conselho(SubtipoConselho subtipo) {
        return new ReferenciaOrganizacao(UUID.randomUUID(), TipoOrganizacao.CONSELHO, subtipo);
    }

    @ParameterizedTest
    @EnumSource(TipoReuniao.class)
    @DisplayName("todo tipo tem regras com ao menos um redator e um convocante aceito")
    void todoTipoTemRegras(TipoReuniao tipo) {
        RegrasTipoReuniao regras = RegrasTipoReuniaoPolicy.para(tipo);

        assertThat(regras.papeisRedatores()).isNotEmpty();
        assertThat(regras.tiposConvocantes()).isNotEmpty();
    }

    @ParameterizedTest
    @EnumSource(value = TipoReuniao.class, names = {"REUNIAO_CPP", "REUNIAO_CPC", "REUNIAO_CAEP",
            "REUNIAO_CONJUNTA_CPP_CAEP", "REUNIAO_PASTORAL", "ASSEMBLEIA_PAROQUIAL", "PLANEJAMENTO_PASTORAL"})
    @DisplayName("tipos de decisão do conselho exigem homologação do Pároco")
    void exigemHomologacao(TipoReuniao tipo) {
        assertThat(RegrasTipoReuniaoPolicy.para(tipo).exigeHomologacao()).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = TipoReuniao.class, names = {"REUNIAO_COORDENADORES", "ASSEMBLEIA_COMUNITARIA",
            "REUNIAO_ORGANISMO", "COMISSAO_ESPECIAL", "REUNIAO_ANIMADORES"})
    @DisplayName("tipos de registro interno dispensam homologação")
    void dispensamHomologacao(TipoReuniao tipo) {
        assertThat(RegrasTipoReuniaoPolicy.para(tipo).exigeHomologacao()).isFalse();
    }

    @ParameterizedTest
    @EnumSource(TipoReuniao.class)
    @DisplayName("o Pároco preside por direito próprio as reuniões nominais de conselho")
    void presidenciaNata(TipoReuniao tipo) {
        boolean esperado = switch (tipo) {
            case REUNIAO_CPP, REUNIAO_CPC, REUNIAO_CAEP, REUNIAO_CONJUNTA_CPP_CAEP, REUNIAO_PASTORAL,
                 REUNIAO_COORDENADORES, REUNIAO_ANIMADORES, PLANEJAMENTO_PASTORAL -> true;
            case ASSEMBLEIA_PAROQUIAL, ASSEMBLEIA_COMUNITARIA, REUNIAO_ORGANISMO, COMISSAO_ESPECIAL -> false;
        };

        assertThat(RegrasTipoReuniaoPolicy.para(tipo).presidenciaNata()).isEqualTo(esperado);
    }

    @Test
    @DisplayName("só os colegiados aplicam quórum e votam")
    void quorumEVotacao() {
        assertThat(RegrasTipoReuniaoPolicy.para(TipoReuniao.REUNIAO_CAEP).aplicaQuorum()).isTrue();
        assertThat(RegrasTipoReuniaoPolicy.para(TipoReuniao.REUNIAO_PASTORAL).aplicaQuorum()).isFalse();
        assertThat(RegrasTipoReuniaoPolicy.para(TipoReuniao.REUNIAO_PASTORAL).permiteVotacao()).isTrue();
        assertThat(RegrasTipoReuniaoPolicy.para(TipoReuniao.REUNIAO_COORDENADORES).permiteVotacao()).isFalse();
        assertThat(RegrasTipoReuniaoPolicy.para(TipoReuniao.ASSEMBLEIA_PAROQUIAL).modoPresenca())
                .isEqualTo(ModoPresenca.QUANTITATIVA);
    }

    @Test
    @DisplayName("convocante aceito segue o subtipo do conselho")
    void convocante() {
        RegrasTipoReuniao cpp = RegrasTipoReuniaoPolicy.para(TipoReuniao.REUNIAO_CPP);

        assertThat(cpp.aceitaConvocante(conselho(SubtipoConselho.CPP))).isTrue();
        assertThat(cpp.aceitaConvocante(conselho(SubtipoConselho.CPC))).isFalse();
        assertThat(cpp.aceitaConvocante(new ReferenciaOrganizacao(UUID.randomUUID(), TipoOrganizacao.PASTORAL, null)))
                .isFalse();
        assertThat(RegrasTipoReuniaoPolicy.para(TipoReuniao.COMISSAO_ESPECIAL)
                .aceitaConvocante(new ReferenciaOrganizacao(UUID.randomUUID(), TipoOrganizacao.PASTORAL, null)))
                .isTrue();
    }

    @Test
    @DisplayName("quórum é a maioria absoluta dos convocados")
    void quorum() {
        ResultadoQuorum cincoDeNove = QuorumPolicy.avaliar(9, 5);
        ResultadoQuorum quatroDeNove = QuorumPolicy.avaliar(9, 4);
        ResultadoQuorum metadeExata = QuorumPolicy.avaliar(8, 4);

        assertThat(cincoDeNove.minimoNecessario()).isEqualTo(5);
        assertThat(cincoDeNove.atingido()).isTrue();
        assertThat(quatroDeNove.atingido()).isFalse();
        assertThat(metadeExata.minimoNecessario()).isEqualTo(5);
        assertThat(metadeExata.atingido()).isFalse();
    }
}
