package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.application.dto.AtaParaImpressao;
import br.com.nsfatima.gestao.conselho.domain.model.AtaAnterior;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.AprovacaoAtaAnterior;
import br.com.nsfatima.gestao.conselho.domain.model.ConteudoAta;
import br.com.nsfatima.gestao.conselho.domain.model.Deliberacao;
import br.com.nsfatima.gestao.conselho.domain.model.ItemPauta;
import br.com.nsfatima.gestao.conselho.domain.model.Presenca;
import br.com.nsfatima.gestao.conselho.domain.model.ResultadoQuorum;
import br.com.nsfatima.gestao.conselho.domain.model.SituacaoPresenca;
import br.com.nsfatima.gestao.conselho.domain.model.Votacao;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Gera o texto corrido de Livro de Atas a partir dos mesmos campos estruturados da ata, para que o
 * layout narrativo não exija redigitação. Função pura: mesma ata, mesmo texto.
 */
public final class NarrativaAtaFormatter {

    private static final String PONTO = ".";
    private static final String PESSOA_DESCONHECIDA = "pessoa não identificada";

    private NarrativaAtaFormatter() {
    }

    /**
     * Parágrafos da ata narrativa, do preâmbulo ao encerramento.
     *
     * @param a ata e os nomes necessários para imprimi-la
     * @return parágrafos em ordem
     */
    public static List<String> paragrafos(AtaParaImpressao a) {
        AtaReuniao ata = a.ata();
        ConteudoAta conteudo = ata.getConteudo();
        List<String> paragrafos = new ArrayList<>();
        paragrafos.add(abertura(a));
        if (a.identificacaoAtaRetificada() != null) {
            paragrafos.add("A presente ata retifica a ata nº " + a.identificacaoAtaRetificada()
                    + ", pelo seguinte motivo: " + comPontoFinal(ata.getJustificativaRetificacao()));
        }
        paragrafos.add(presenca(conteudo, a.nomesFieis()));
        ata.quorum().ifPresent(q -> paragrafos.add(quorum(q)));
        if (conteudo.ataAnterior().aprovacao() != AprovacaoAtaAnterior.NAO_APLICAVEL) {
            paragrafos.add(ataAnterior(conteudo.ataAnterior()));
        }
        for (ItemPauta item : conteudo.itens()) {
            paragrafos.addAll(item(item, a.nomesFieis()));
        }
        paragrafos.add(encerramento(ata));
        return paragrafos;
    }

    private static String abertura(AtaParaImpressao a) {
        ConteudoAta c = a.ata().getConteudo();
        String orgaos = a.organizacaoAssociada() == null
                ? a.organizacao() : a.organizacao() + " e " + a.organizacaoAssociada();
        return RotulosAta.dataPorExtenso(c.dataReuniao()) + ", no local " + c.local() + ", realizou-se, em caráter "
                + RotulosAta.carater(c.carater()) + " e de forma " + RotulosAta.modalidade(c.modalidade())
                + ", a reunião registrada como “" + a.ata().getTipo().getRotulo() + "” de " + orgaos + PONTO;
    }

    private static String presenca(ConteudoAta c, Map<UUID, String> nomes) {
        if (!c.presenca().ehNominal()) {
            Integer total = c.presenca().total();
            return total == null ? "A presença não foi informada."
                    : "Registrou-se a participação de " + total + " pessoa" + (total == 1 ? "" : "s") + PONTO;
        }
        List<String> frases = new ArrayList<>();
        adicionar(frases, "Estiveram presentes: ", pessoas(c, SituacaoPresenca.PRESENTE, nomes));
        adicionar(frases, "Justificaram a ausência: ", pessoas(c, SituacaoPresenca.AUSENTE_JUSTIFICADA, nomes));
        adicionar(frases, "Ausentes: ", pessoas(c, SituacaoPresenca.AUSENTE, nomes));
        return String.join(" ", frases);
    }

    private static void adicionar(List<String> frases, String prefixo, List<String> pessoas) {
        if (!pessoas.isEmpty()) {
            frases.add(prefixo + RotulosAta.listar(pessoas) + PONTO);
        }
    }

    private static List<String> pessoas(ConteudoAta c, SituacaoPresenca situacao, Map<UUID, String> nomes) {
        return c.presenca().nominal().stream().filter(p -> p.situacao() == situacao).map(p -> descrever(p, nomes))
                .toList();
    }

    private static String descrever(Presenca presenca, Map<UUID, String> nomes) {
        String nome = nomes.getOrDefault(presenca.fielId(), PESSOA_DESCONHECIDA);
        return presenca.funcao() == null ? nome : nome + " (" + presenca.funcao() + ")";
    }

    private static String quorum(ResultadoQuorum q) {
        return "Apurou-se a presença de " + q.presentes() + " dos " + q.membrosConvocados()
                + " membros convocados; o quórum, de " + q.minimoNecessario() + " membros, "
                + (q.atingido() ? "foi atingido." : "não foi atingido.");
    }

    private static String ataAnterior(AtaAnterior anterior) {
        return anterior.aprovacao() == AprovacaoAtaAnterior.APROVADA
                ? "Lida a ata da reunião anterior, foi aprovada."
                : "Lida a ata da reunião anterior, foi aprovada com as seguintes ressalvas: "
                        + comPontoFinal(anterior.ressalvas());
    }

    private static List<String> item(ItemPauta item, Map<UUID, String> nomes) {
        List<String> paragrafos = new ArrayList<>();
        String relato = item.relato() == null ? "tratou-se do assunto." : comPontoFinal(item.relato());
        paragrafos.add("No item " + item.ordem() + " da pauta (" + item.titulo() + "): " + relato);
        for (Deliberacao d : item.deliberacoes()) {
            paragrafos.add("Deliberação " + item.ordem() + PONTO + d.ordem() + ": " + comPontoFinal(d.texto())
                    + " Resultado: " + RotulosAta.resultado(d.resultado()).toLowerCase(Locale.ROOT)
                    + votos(d.votacao()) + PONTO);
            d.conflitos().forEach(c -> paragrafos.add(nomes.getOrDefault(c.declaranteFielId(), PESSOA_DESCONHECIDA)
                    + " declarou conflito de interesses: " + comPontoFinal(c.descricao())));
        }
        return paragrafos;
    }

    private static String votos(Votacao votacao) {
        return votacao == null ? "" : " (votos: " + votacao.favor() + " favoráveis, " + votacao.contra()
                + " contrários, " + votacao.abstencoes() + " abstenções)";
    }

    private static String encerramento(AtaReuniao ata) {
        String base = "Nada mais havendo a tratar, lavrou-se a presente ata, que, lida e achada conforme, "
                + "vai assinada pelo redator";
        return switch (ata.getStatus()) {
            case HOMOLOGADA -> base + " e foi homologada pelo Pároco em "
                    + RotulosAta.data(RotulosAta.dataLocal(ata.getDecididoEmUtc())) + PONTO;
            case REGISTRADA -> base + " e foi registrada em "
                    + RotulosAta.data(RotulosAta.dataLocal(ata.getDecididoEmUtc())) + PONTO;
            case RASCUNHO, PENDENTE_HOMOLOGACAO, VETADA -> base + ". Minuta sem efeito até "
                    + (ata.regras().exigeHomologacao() ? "a homologação do Pároco." : "o registro.");
        };
    }

    private static String comPontoFinal(String texto) {
        String limpo = texto.trim();
        return limpo.endsWith(PONTO) ? limpo : limpo + PONTO;
    }
}
