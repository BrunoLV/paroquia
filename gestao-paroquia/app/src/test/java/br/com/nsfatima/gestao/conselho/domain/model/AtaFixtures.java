package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Construtores de cenário compartilhados pelos testes do contexto de atas. */
public final class AtaFixtures {

    public static final Instant AGORA = Instant.parse("2026-06-01T12:00:00Z");
    public static final UUID FIEL_A = UUID.randomUUID();
    public static final UUID FIEL_B = UUID.randomUUID();
    public static final UUID FIEL_C = UUID.randomUUID();

    private AtaFixtures() {
    }

    public static ReferenciaOrganizacao conselho(SubtipoConselho subtipo) {
        return new ReferenciaOrganizacao(UUID.randomUUID(), TipoOrganizacao.CONSELHO, subtipo);
    }

    public static ReferenciaOrganizacao pastoral() {
        return new ReferenciaOrganizacao(UUID.randomUUID(), TipoOrganizacao.PASTORAL, null);
    }

    public static PresencaReuniao tresPresentes() {
        return PresencaReuniao.nominal(List.of(
                new Presenca(FIEL_A, null, SituacaoPresenca.PRESENTE),
                new Presenca(FIEL_B, "Coordenador(a)", SituacaoPresenca.PRESENTE),
                new Presenca(FIEL_C, null, SituacaoPresenca.AUSENTE_JUSTIFICADA)));
    }

    public static Deliberacao deliberacao(int ordem) {
        return new Deliberacao(ordem, "Aprovar a reforma do telhado", ResultadoDeliberacao.PARECER_FAVORAVEL,
                null, List.of());
    }

    public static ItemPauta item(int ordem, Deliberacao... deliberacoes) {
        return new ItemPauta(ordem, "Reforma do telhado", "Discutiu-se o orçamento.", List.of(deliberacoes));
    }

    public static ConteudoAta conteudo(PresencaReuniao presenca, ItemPauta... itens) {
        return new ConteudoAta("Reunião de junho", LocalDate.of(2026, 6, 1), "Salão paroquial",
                CaraterReuniao.ORDINARIA, ModalidadeReuniao.PRESENCIAL, presenca, AtaAnterior.nenhuma(),
                List.of(itens));
    }

    public static ConteudoAta conteudoCompleto() {
        return conteudo(tresPresentes(), item(1, deliberacao(1)));
    }

    public static AtaReuniao rascunhoDeCpp() {
        return AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.REUNIAO_CPP,
                conselho(SubtipoConselho.CPP), null, conteudoCompleto(), "secretario-1", AGORA);
    }

    public static AtaReuniao homologadaDeCpp() {
        AtaReuniao ata = rascunhoDeCpp();
        ata.submeter(5, (org, ano) -> 1, AGORA);
        ata.homologar("paroco-1", (org, ano) -> 1, AGORA);
        return ata;
    }

    public static AtaReuniao rascunhoDeComissao() {
        return AtaReuniao.rascunhar(AtaReuniaoId.gerar(), TipoReuniao.COMISSAO_ESPECIAL, pastoral(), null,
                conteudoCompleto(), "coordenador-1", AGORA);
    }
}
