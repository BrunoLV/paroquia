package br.com.nsfatima.gestao.conselho.domain.policy;

import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.iam.domain.model.PapelGlobal;
import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AutorizacaoAtaPolicyTest {

    private static final RegrasTipoReuniao CPP = RegrasTipoReuniaoPolicy.para(TipoReuniao.REUNIAO_CPP);

    private static AtorOrganizacao ator(Set<PapelGlobal> globais, Set<PapelOrganizacional> locais) {
        return new AtorOrganizacao(globais, locais);
    }

    @Test
    @DisplayName("quem não tem papel global de leitura nem vínculo com o órgão não faz nada")
    void semAcesso() {
        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(ator(Set.of(PapelGlobal.FIEL), Set.of()), CPP,
                StatusAta.HOMOLOGADA)).isEmpty();
    }

    @Test
    @DisplayName("ADMIN não lê conteúdo de ata")
    void adminNaoLe() {
        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(ator(Set.of(PapelGlobal.ADMIN), Set.of()), CPP,
                StatusAta.PENDENTE_HOMOLOGACAO)).isEmpty();
    }

    @Test
    @DisplayName("secretário do conselho redige e submete rascunho, mas não homologa")
    void secretarioRedige() {
        AtorOrganizacao secretario = ator(Set.of(), Set.of(PapelOrganizacional.SECRETARIO_CONSELHO));

        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(secretario, CPP, StatusAta.RASCUNHO))
                .containsExactlyInAnyOrder(AcaoAta.LER, AcaoAta.EXPORTAR, AcaoAta.REDIGIR, AcaoAta.SUBMETER);
        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(secretario, CPP, StatusAta.PENDENTE_HOMOLOGACAO))
                .containsExactlyInAnyOrder(AcaoAta.LER, AcaoAta.EXPORTAR);
    }

    @Test
    @DisplayName("Pároco homologa e veta atas pendentes, lê e vê a trilha, mas não redige")
    void parocoHomologa() {
        AtorOrganizacao paroco = ator(Set.of(PapelGlobal.PAROCO), Set.of());

        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(paroco, CPP, StatusAta.PENDENTE_HOMOLOGACAO))
                .containsExactlyInAnyOrder(AcaoAta.LER, AcaoAta.EXPORTAR, AcaoAta.VER_TRILHA, AcaoAta.HOMOLOGAR,
                        AcaoAta.VETAR);
        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(paroco, CPP, StatusAta.RASCUNHO))
                .doesNotContain(AcaoAta.REDIGIR, AcaoAta.SUBMETER);
    }

    @Test
    @DisplayName("membro comum lê e exporta; não redige")
    void membroLe() {
        AtorOrganizacao membro = ator(Set.of(), Set.of(PapelOrganizacional.MEMBRO_CONSELHO_PASTORAL));

        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(membro, CPP, StatusAta.RASCUNHO))
                .containsExactlyInAnyOrder(AcaoAta.LER, AcaoAta.EXPORTAR);
    }

    @Test
    @DisplayName("redator reabre ata vetada e retifica ata selada")
    void reabreERetifica() {
        AtorOrganizacao secretario = ator(Set.of(), Set.of(PapelOrganizacional.SECRETARIO_CONSELHO));

        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(secretario, CPP, StatusAta.VETADA))
                .contains(AcaoAta.REABRIR);
        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(secretario, CPP, StatusAta.HOMOLOGADA))
                .contains(AcaoAta.RETIFICAR);
        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(secretario, CPP, StatusAta.REGISTRADA))
                .contains(AcaoAta.RETIFICAR);
    }

    @Test
    @DisplayName("o redator depende do tipo: coordenador redige reunião de organismo, não a do CPP")
    void redatorPorTipo() {
        AtorOrganizacao coordenador = ator(Set.of(), Set.of(PapelOrganizacional.COORDENADOR));
        RegrasTipoReuniao organismo = RegrasTipoReuniaoPolicy.para(TipoReuniao.REUNIAO_ORGANISMO);

        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(coordenador, organismo, StatusAta.RASCUNHO))
                .contains(AcaoAta.REDIGIR);
        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(coordenador, CPP, StatusAta.RASCUNHO))
                .doesNotContain(AcaoAta.REDIGIR);
    }

    @Test
    @DisplayName("na reunião conjunta, o secretário do órgão associado só lê: redigir é do convocante")
    void orgaoAssociadoSoLe() {
        RegrasTipoReuniao conjunta = RegrasTipoReuniaoPolicy.para(TipoReuniao.REUNIAO_CONJUNTA_CPP_CAEP);
        AtorOrganizacao secretarioDoCaep = new AtorOrganizacao(Set.of(), Set.of(),
                Set.of(PapelOrganizacional.SECRETARIO_CONSELHO));

        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(secretarioDoCaep, conjunta, StatusAta.RASCUNHO))
                .containsExactlyInAnyOrder(AcaoAta.LER, AcaoAta.EXPORTAR);
    }

    @Test
    @DisplayName("secretaria paroquial lê qualquer órgão e vê a trilha")
    void secretariaParoquial() {
        AtorOrganizacao secretaria = ator(Set.of(PapelGlobal.SECRETARIA_PAROQUIAL), Set.of());

        assertThat(AutorizacaoAtaPolicy.acoesPermitidas(secretaria, CPP, StatusAta.HOMOLOGADA))
                .containsExactlyInAnyOrder(AcaoAta.LER, AcaoAta.EXPORTAR, AcaoAta.VER_TRILHA);
        assertThat(AutorizacaoAtaPolicy.leituraEmQualquerOrganizacao(Set.of(PapelGlobal.SECRETARIA_PAROQUIAL)))
                .isTrue();
        assertThat(AutorizacaoAtaPolicy.leituraEmQualquerOrganizacao(Set.of(PapelGlobal.ADMIN, PapelGlobal.FIEL)))
                .isFalse();
    }
}
