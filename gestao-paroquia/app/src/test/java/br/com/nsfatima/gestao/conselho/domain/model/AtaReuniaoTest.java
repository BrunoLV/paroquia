package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaImutavelException;
import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;
import br.com.nsfatima.gestao.conselho.domain.exception.TipoReuniaoIncompativelException;
import br.com.nsfatima.gestao.conselho.domain.exception.TransicaoAtaInvalidaException;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.AGORA;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.FIEL_A;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.FIEL_C;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.conselho;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.conteudo;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.conteudoCompleto;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.deliberacao;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.homologadaDeCpp;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.item;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.pastoral;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.rascunhoDeComissao;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.rascunhoDeCpp;
import static br.com.nsfatima.gestao.conselho.domain.model.AtaFixtures.tresPresentes;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtaReuniaoTest {

    private static final NumeradorAta PRIMEIRA = (org, ano) -> 1;

    @Test
    @DisplayName("nasce rascunho, sem número, com minuta")
    void nasceRascunho() {
        AtaReuniao ata = rascunhoDeCpp();

        assertThat(ata.getStatus()).isEqualTo(StatusAta.RASCUNHO);
        assertThat(ata.identificacao()).isEmpty();
    }

    @Test
    @DisplayName("rejeita convocante incompatível com o tipo")
    void rejeitaConvocanteIncompativel() {
        assertThatThrownBy(() -> AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP,
                conselho(SubtipoConselho.CAEP), null, conteudoCompleto(), "s", AGORA))
                .isInstanceOf(TipoReuniaoIncompativelException.class);
    }

    @Test
    @DisplayName("reunião conjunta exige o CAEP como organização associada")
    void conjuntaExigeCaep() {
        ReferenciaOrganizacao cpp = conselho(SubtipoConselho.CPP);

        assertThatThrownBy(() -> AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CONJUNTA_CPP_CAEP,
                cpp, null, conteudoCompleto(), "s", AGORA)).isInstanceOf(TipoReuniaoIncompativelException.class);
        AtaReuniao conjunta = AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CONJUNTA_CPP_CAEP,
                cpp, conselho(SubtipoConselho.CAEP), conteudoCompleto(), "s", AGORA);
        assertThat(conjunta.getOrganizacaoAssociadaId()).isNotNull();
    }

    @Test
    @DisplayName("tipo sem associada rejeita organização associada")
    void tipoSemAssociadaRejeita() {
        assertThatThrownBy(() -> AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP,
                conselho(SubtipoConselho.CPP), conselho(SubtipoConselho.CAEP), conteudoCompleto(), "s", AGORA))
                .isInstanceOf(TipoReuniaoIncompativelException.class);
    }

    @Test
    @DisplayName("submeter tipo com homologação vai a pendente e congela o quórum")
    void submeterVaiAPendente() {
        AtaReuniao ata = rascunhoDeCpp();

        ata.submeter(5, PRIMEIRA, AGORA);

        assertThat(ata.getStatus()).isEqualTo(StatusAta.PENDENTE_HOMOLOGACAO);
        assertThat(ata.quorum()).hasValueSatisfying(q -> {
            assertThat(q.membrosConvocados()).as("5 membros e o Pároco, presidente nato").isEqualTo(6);
            assertThat(q.minimoNecessario()).isEqualTo(4);
            assertThat(q.presentes()).isEqualTo(2);
            assertThat(q.atingido()).isFalse();
        });
    }

    @Test
    @DisplayName("submeter tipo sem homologação registra, numera e sela de imediato")
    void submeterSemHomologacaoRegistra() {
        AtaReuniao ata = rascunhoDeComissao();

        ata.submeter(null, PRIMEIRA, AGORA);

        assertThat(ata.getStatus()).isEqualTo(StatusAta.REGISTRADA);
        assertThat(ata.identificacao()).contains("001/2026");
        assertThat(ata.quorum()).isEmpty();
    }

    @Test
    @DisplayName("não submete ata sem item de pauta ou sem presença")
    void naoSubmeteIncompleta() {
        AtaReuniao semPauta = AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP,
                conselho(SubtipoConselho.CPP), null, conteudo(tresPresentes()), "s", AGORA);
        AtaReuniao semPresenca = AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP,
                conselho(SubtipoConselho.CPP), null, conteudo(PresencaReuniao.vazia(), item(1)), "s", AGORA);

        assertThatThrownBy(() -> semPauta.submeter(5, PRIMEIRA, AGORA)).isInstanceOf(AtaInvalidaException.class);
        assertThatThrownBy(() -> semPresenca.submeter(5, PRIMEIRA, AGORA)).isInstanceOf(AtaInvalidaException.class);
    }

    @Test
    @DisplayName("colegiado sem membros ativos não pode apurar quórum")
    void colegiadoSemMembros() {
        assertThatThrownBy(() -> rascunhoDeCpp().submeter(0, PRIMEIRA, AGORA))
                .isInstanceOf(AtaInvalidaException.class);
    }

    @Test
    @DisplayName("homologar sela, numera e registra o homologador")
    void homologarSela() {
        AtaReuniao ata = homologadaDeCpp();

        assertThat(ata.getStatus()).isEqualTo(StatusAta.HOMOLOGADA);
        assertThat(ata.getHomologadorId()).isEqualTo("paroco-1");
        assertThat(ata.identificacao()).contains("001/2026");
    }

    @Test
    @DisplayName("só decide ata pendente")
    void soDecidePendente() {
        AtaReuniao rascunho = rascunhoDeCpp();

        assertThatThrownBy(() -> rascunho.homologar("p", PRIMEIRA, AGORA))
                .isInstanceOf(TransicaoAtaInvalidaException.class);
        assertThatThrownBy(() -> homologadaDeCpp().vetar("p", "motivo", AGORA))
                .isInstanceOf(AtaImutavelException.class);
    }

    @Test
    @DisplayName("veto exige motivo e a ata reaberta preserva o motivo até nova submissão")
    void vetoEReabertura() {
        AtaReuniao ata = rascunhoDeCpp();
        ata.submeter(5, PRIMEIRA, AGORA);

        assertThatThrownBy(() -> ata.vetar("p", "  ", AGORA)).isInstanceOf(AtaInvalidaException.class);
        ata.vetar("p", "Faltou orçamento", AGORA);
        ata.reabrir();

        assertThat(ata.getStatus()).isEqualTo(StatusAta.RASCUNHO);
        assertThat(ata.getMotivoVeto()).isEqualTo("Faltou orçamento");
        ata.submeter(5, PRIMEIRA, AGORA);
        assertThat(ata.getMotivoVeto()).isNull();
    }

    @Test
    @DisplayName("ata selada é imutável")
    void selada() {
        assertThatThrownBy(() -> homologadaDeCpp().redigir(conteudoCompleto()))
                .isInstanceOf(AtaImutavelException.class);
    }

    @Test
    @DisplayName("ata submetida não admite edição")
    void submetidaNaoEdita() {
        AtaReuniao ata = rascunhoDeCpp();
        ata.submeter(5, PRIMEIRA, AGORA);

        assertThatThrownBy(() -> ata.redigir(conteudoCompleto())).isInstanceOf(TransicaoAtaInvalidaException.class);
    }

    @Test
    @DisplayName("retificação nasce da ata selada, em rascunho, ligada à original")
    void retificacao() {
        AtaReuniao original = homologadaDeCpp();

        AtaReuniao retificadora = original.abrirRetificacao(AtaReuniaoId.gerar(), "Erro de grafia", "s2", AGORA);

        assertThat(retificadora.getStatus()).isEqualTo(StatusAta.RASCUNHO);
        assertThat(retificadora.getAtaRetificadaId()).isEqualTo(original.getId());
        assertThat(retificadora.getTipo()).isEqualTo(original.getTipo());
        assertThat(original.getStatus()).isEqualTo(StatusAta.HOMOLOGADA);
    }

    @Test
    @DisplayName("rascunho não pode ser retificado e a retificação exige justificativa")
    void retificacaoInvalida() {
        assertThatThrownBy(() -> rascunhoDeCpp().abrirRetificacao(AtaReuniaoId.gerar(), "x", "s", AGORA))
                .isInstanceOf(TransicaoAtaInvalidaException.class);
        assertThatThrownBy(() -> homologadaDeCpp().abrirRetificacao(AtaReuniaoId.gerar(), " ", "s", AGORA))
                .isInstanceOf(AtaInvalidaException.class);
    }

    @Test
    @DisplayName("votação só em tipo que vota e limitada aos presentes sem conflito")
    void votacao() {
        Deliberacao comVotos = new Deliberacao(1, "Aprovar", ResultadoDeliberacao.PARECER_FAVORAVEL,
                new Votacao(2, 0, 0), List.of());
        Deliberacao votosDemais = new Deliberacao(1, "Aprovar", ResultadoDeliberacao.PARECER_FAVORAVEL,
                new Votacao(3, 0, 0), List.of());
        Deliberacao conflito = new Deliberacao(1, "Aprovar", ResultadoDeliberacao.PARECER_FAVORAVEL,
                new Votacao(2, 0, 0), List.of(new ConflitoInteresse(FIEL_A, "Parente do fornecedor")));
        ReferenciaOrganizacao cpp = conselho(SubtipoConselho.CPP);

        assertThat(AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP, cpp, null,
                conteudo(tresPresentes(), item(1, comVotos)), "s", AGORA)).isNotNull();
        assertThatThrownBy(() -> AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP, cpp, null,
                conteudo(tresPresentes(), item(1, votosDemais)), "s", AGORA)).isInstanceOf(AtaInvalidaException.class);
        assertThatThrownBy(() -> AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP, cpp, null,
                conteudo(tresPresentes(), item(1, conflito)), "s", AGORA)).isInstanceOf(AtaInvalidaException.class);
        assertThatThrownBy(() -> AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.COMISSAO_ESPECIAL,
                pastoral(), null, conteudo(tresPresentes(), item(1, comVotos)), "s", AGORA))
                .isInstanceOf(AtaInvalidaException.class);
    }

    @Test
    @DisplayName("declarante de conflito deve estar presente")
    void declaranteAusente() {
        Deliberacao ausente = new Deliberacao(1, "Aprovar", ResultadoDeliberacao.PARECER_FAVORAVEL, null,
                List.of(new ConflitoInteresse(FIEL_C, "Interesse direto")));

        assertThatThrownBy(() -> AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP,
                conselho(SubtipoConselho.CPP), null, conteudo(tresPresentes(), item(1, ausente)), "s", AGORA))
                .isInstanceOf(AtaInvalidaException.class);
    }

    @Test
    @DisplayName("modo de presença segue o tipo")
    void modoDePresenca() {
        assertThatThrownBy(() -> AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.ASSEMBLEIA_COMUNITARIA,
                conselho(SubtipoConselho.CPC), null, conteudoCompleto(), "s", AGORA))
                .isInstanceOf(AtaInvalidaException.class);
        assertThatThrownBy(() -> AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP,
                conselho(SubtipoConselho.CPP), null,
                conteudo(PresencaReuniao.quantitativa(40), item(1, deliberacao(1))), "s", AGORA))
                .isInstanceOf(AtaInvalidaException.class);
    }

    @Test
    @DisplayName("assembleia comunitária registra total de participantes e sela sem homologação")
    void assembleiaComunitaria() {
        AtaReuniao ata = AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.ASSEMBLEIA_COMUNITARIA,
                conselho(SubtipoConselho.CPC), null,
                conteudo(PresencaReuniao.quantitativa(40), item(1)), "s", AGORA);

        ata.submeter(null, PRIMEIRA, AGORA);

        assertThat(ata.getStatus()).isEqualTo(StatusAta.REGISTRADA);
    }

    @Test
    @DisplayName("reconstituir revalida a coerência entre selagem e número")
    void reconstituirIncoerente() {
        AtaReuniao.Estado semNumero = new AtaReuniao.Estado(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP,
                UUID.randomUUID(), null, "s", AGORA, null, null, conteudoCompleto(), StatusAta.HOMOLOGADA, null,
                null, 5, "p", null, AGORA);

        assertThatThrownBy(() -> AtaReuniao.reconstituir(semNumero)).isInstanceOf(AtaInvalidaException.class);
    }
}
