package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.application.dto.AtaParaImpressao;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Assinaturas;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Destaque;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Paragrafo;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.QuebraDePagina;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Tabela;
import br.com.nsfatima.gestao.conselho.application.dto.BlocoDocumento.Titulo;
import br.com.nsfatima.gestao.conselho.application.dto.CabecalhoParoquia;
import br.com.nsfatima.gestao.conselho.application.dto.ConteudoExportacao;
import br.com.nsfatima.gestao.conselho.application.dto.DocumentoReuniao;
import br.com.nsfatima.gestao.conselho.application.dto.LayoutExportacao;
import br.com.nsfatima.gestao.conselho.domain.model.AprovacaoAtaAnterior;
import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ConteudoAta;
import br.com.nsfatima.gestao.conselho.domain.model.Deliberacao;
import br.com.nsfatima.gestao.conselho.domain.model.ItemPauta;
import br.com.nsfatima.gestao.conselho.domain.model.Presenca;
import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Compõe o {@link DocumentoReuniao} de uma ata, ou de uma compilação delas, em blocos neutros que os
 * exportadores desenham. Concentra aqui o que o documento diz, para que PDF e Word nunca divirjam. Função pura.
 *
 * <p>O documento só leva o carimbo de homologação (e a linha do Pároco) quando a ata está de fato selada; antes
 * disso sai como minuta, com a advertência de que não produz efeito.</p>
 */
public final class ComposicaoDocumentoAta {

    private static final String PONTO = ".";
    private static final String ADVERTENCIA_MINUTA = "MINUTA — SEM EFEITO ATÉ A HOMOLOGAÇÃO OU O REGISTRO";
    private static final String PESSOA_DESCONHECIDA = "Pessoa não identificada";
    private static final DateTimeFormatter EMISSAO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");

    private ComposicaoDocumentoAta() {
    }

    /**
     * Compõe o documento.
     *
     * @param cabecalho identidade institucional impressa no topo
     * @param atas atas a incluir, ao menos uma
     * @param layout estruturado ou narrativo
     * @param conteudo ata completa ou só o quadro de decisões
     * @param emissao instante de emissão, no fuso de Brasília
     * @return documento pronto para ser desenhado
     */
    public static DocumentoReuniao compor(CabecalhoParoquia cabecalho, List<AtaParaImpressao> atas,
                                          LayoutExportacao layout, ConteudoExportacao conteudo,
                                          ZonedDateTime emissao) {
        boolean compilacao = atas.size() > 1;
        List<BlocoDocumento> blocos = new ArrayList<>();
        if (compilacao) {
            blocos.add(new Titulo("Livro de atas e decisões", 1));
        }
        for (int i = 0; i < atas.size(); i++) {
            if (compilacao && i > 0) {
                blocos.add(new QuebraDePagina());
            }
            blocos.addAll(blocosDaAta(atas.get(i), layout, conteudo, compilacao ? 2 : 1));
        }
        boolean minuta = atas.stream().anyMatch(a -> !a.ata().getStatus().selada());
        String titulo = compilacao ? "Livro de atas e decisões" : tituloDe(atas.get(0), conteudo);
        return new DocumentoReuniao(cabecalho, titulo, minuta, "Documento emitido em " + EMISSAO.format(emissao),
                blocos);
    }

    private static String tituloDe(AtaParaImpressao a, ConteudoExportacao conteudo) {
        String base = a.ata().getTipo().getTituloDaAta();
        return conteudo == ConteudoExportacao.SOMENTE_DECISOES ? "Decisões — " + base : base;
    }

    private static List<BlocoDocumento> blocosDaAta(AtaParaImpressao a, LayoutExportacao layout,
                                                    ConteudoExportacao conteudo, int nivel) {
        AtaReuniao ata = a.ata();
        List<BlocoDocumento> blocos = new ArrayList<>();
        blocos.add(new Titulo(tituloDe(a, conteudo) + " — " + identificacao(ata), nivel));
        blocos.add(new Paragrafo(a.organizacaoAssociada() == null
                ? a.organizacao() : a.organizacao() + " e " + a.organizacaoAssociada()));
        if (!ata.getStatus().selada()) {
            blocos.add(new Destaque(ADVERTENCIA_MINUTA));
        }
        if (conteudo == ConteudoExportacao.SOMENTE_DECISOES) {
            blocos.add(quadroDeDecisoes(ata.getConteudo()));
        } else if (layout == LayoutExportacao.NARRATIVA) {
            NarrativaAtaFormatter.paragrafos(a).forEach(texto -> blocos.add(new Paragrafo(texto)));
        } else {
            blocos.addAll(estruturada(a));
        }
        blocos.addAll(fecho(ata, conteudo));
        return blocos;
    }

    private static String identificacao(AtaReuniao ata) {
        return ata.identificacao().map(numero -> "nº " + numero).orElse("Minuta");
    }

    private static List<BlocoDocumento> estruturada(AtaParaImpressao a) {
        AtaReuniao ata = a.ata();
        ConteudoAta c = ata.getConteudo();
        List<BlocoDocumento> blocos = new ArrayList<>();
        if (a.identificacaoAtaRetificada() != null) {
            blocos.add(new Paragrafo("Esta ata retifica a ata nº " + a.identificacaoAtaRetificada() + ". Motivo: "
                    + ata.getJustificativaRetificacao()));
        }
        blocos.add(dadosDaReuniao(c));
        blocos.add(new Titulo("Presença", 3));
        blocos.addAll(presenca(a));
        ata.quorum().ifPresent(q -> blocos.add(new Paragrafo("Quórum: " + q.presentes() + " de "
                + q.membrosConvocados() + " membros convocados presentes (mínimo de " + q.minimoNecessario()
                + "): " + (q.atingido() ? "quórum atingido." : "quórum não atingido."))));
        if (c.ataAnterior().aprovacao() != AprovacaoAtaAnterior.NAO_APLICAVEL) {
            blocos.add(new Paragrafo("Ata da reunião anterior: " + (c.ataAnterior().aprovacao()
                    == AprovacaoAtaAnterior.APROVADA ? "aprovada." : "aprovada com ressalvas: "
                    + c.ataAnterior().ressalvas())));
        }
        c.itens().forEach(item -> blocos.addAll(item(item, a.nomesFieis())));
        return blocos;
    }

    private static Tabela dadosDaReuniao(ConteudoAta c) {
        return new Tabela(List.of(), List.of(
                List.of("Reunião", c.titulo()),
                List.of("Data", RotulosAta.data(c.dataReuniao())),
                List.of("Local", c.local()),
                List.of("Caráter", RotulosAta.carater(c.carater())),
                List.of("Modalidade", RotulosAta.modalidade(c.modalidade()))));
    }

    private static List<BlocoDocumento> presenca(AtaParaImpressao a) {
        ConteudoAta c = a.ata().getConteudo();
        if (!c.presenca().ehNominal()) {
            Integer total = c.presenca().total();
            return List.of(new Paragrafo(total == null
                    ? "Presença não informada." : "Total de participantes: " + total + PONTO));
        }
        List<List<String>> linhas = c.presenca().nominal().stream()
                .map(p -> linhaDePresenca(p, a.nomesFieis())).toList();
        return List.of(new Tabela(List.of("Nome", "Função", "Situação"), linhas));
    }

    private static List<String> linhaDePresenca(Presenca p, Map<UUID, String> nomes) {
        return List.of(nomes.getOrDefault(p.fielId(), PESSOA_DESCONHECIDA), p.funcao() == null ? "—" : p.funcao(),
                RotulosAta.situacao(p.situacao()));
    }

    private static List<BlocoDocumento> item(ItemPauta item, Map<UUID, String> nomes) {
        List<BlocoDocumento> blocos = new ArrayList<>();
        blocos.add(new Titulo(item.ordem() + ". " + item.titulo(), 3));
        if (item.relato() != null) {
            blocos.add(new Paragrafo(item.relato()));
        }
        for (Deliberacao d : item.deliberacoes()) {
            blocos.add(new Paragrafo("Deliberação " + item.ordem() + PONTO + d.ordem() + ": " + d.texto()
                    + " — " + RotulosAta.resultado(d.resultado()) + votos(d)));
            d.conflitos().forEach(c -> blocos.add(new Paragrafo("Conflito de interesses declarado por "
                    + nomes.getOrDefault(c.declaranteFielId(), PESSOA_DESCONHECIDA) + ": " + c.descricao())));
        }
        return blocos;
    }

    private static String votos(Deliberacao d) {
        return d.votacao() == null ? "" : " (favoráveis: " + d.votacao().favor() + "; contrários: "
                + d.votacao().contra() + "; abstenções: " + d.votacao().abstencoes() + ")";
    }

    private static Tabela quadroDeDecisoes(ConteudoAta c) {
        List<List<String>> linhas = new ArrayList<>();
        for (ItemPauta item : c.itens()) {
            for (Deliberacao d : item.deliberacoes()) {
                linhas.add(List.of(item.ordem() + PONTO + d.ordem() + " " + item.titulo(), d.texto(),
                        RotulosAta.resultado(d.resultado()), d.votacao() == null ? "—" : votos(d).trim()));
            }
        }
        return new Tabela(List.of("Item", "Decisão", "Resultado", "Votação"), linhas);
    }

    private static List<BlocoDocumento> fecho(AtaReuniao ata, ConteudoExportacao conteudo) {
        List<BlocoDocumento> blocos = new ArrayList<>();
        StatusAta status = ata.getStatus();
        if (status == StatusAta.HOMOLOGADA) {
            blocos.add(new Paragrafo("Homologada pelo Pároco em "
                    + RotulosAta.data(RotulosAta.dataLocal(ata.getDecididoEmUtc())) + PONTO));
        } else if (status == StatusAta.REGISTRADA) {
            blocos.add(new Paragrafo("Registrada em "
                    + RotulosAta.data(RotulosAta.dataLocal(ata.getDecididoEmUtc())) + PONTO));
        }
        if (conteudo == ConteudoExportacao.ATA_COMPLETA) {
            List<String> rotulos = new ArrayList<>(List.of("Redator"));
            if (ata.regras().exigeHomologacao()) {
                rotulos.add("Pároco");
            }
            blocos.add(new Assinaturas(rotulos));
        }
        return blocos;
    }
}
