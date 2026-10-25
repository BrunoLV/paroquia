package br.com.nsfatima.gestao.conselho.application.service;

import br.com.nsfatima.gestao.conselho.domain.model.CaraterReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ModalidadeReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ResultadoDeliberacao;
import br.com.nsfatima.gestao.conselho.domain.model.SituacaoPresenca;
import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/** Textos em português dos valores do domínio, para os documentos exportados. */
public final class RotulosAta {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter DATA_BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private RotulosAta() {
    }

    public static String resultado(ResultadoDeliberacao resultado) {
        return switch (resultado) {
            case PARECER_FAVORAVEL -> "Parecer favorável";
            case PARECER_CONTRARIO -> "Parecer contrário";
            case ADIADA -> "Adiada";
            case INFORMATIVA -> "Informativa";
        };
    }

    public static String situacao(SituacaoPresenca situacao) {
        return switch (situacao) {
            case PRESENTE -> "Presente";
            case AUSENTE_JUSTIFICADA -> "Ausência justificada";
            case AUSENTE -> "Ausente";
        };
    }

    public static String carater(CaraterReuniao carater) {
        return carater == CaraterReuniao.ORDINARIA ? "ordinário" : "extraordinário";
    }

    public static String modalidade(ModalidadeReuniao modalidade) {
        return switch (modalidade) {
            case PRESENCIAL -> "presencial";
            case VIRTUAL -> "virtual";
            case HIBRIDA -> "híbrida";
        };
    }

    public static String status(StatusAta status) {
        return switch (status) {
            case RASCUNHO -> "Rascunho";
            case PENDENTE_HOMOLOGACAO -> "Pendente de homologação";
            case HOMOLOGADA -> "Homologada";
            case VETADA -> "Vetada";
            case REGISTRADA -> "Registrada";
        };
    }

    /** Data civil, no fuso de Brasília, em que ocorreu o instante. */
    public static LocalDate dataLocal(Instant instante) {
        return instante.atZone(BRASILIA).toLocalDate();
    }

    /** Data no formato {@code dd/MM/yyyy}. */
    public static String data(LocalDate data) {
        return data.format(DATA_BR);
    }

    /** Abertura de ata narrativa: "Aos 12 dias do mês de março de 2026" (ou "No 1º dia ..."). */
    public static String dataPorExtenso(LocalDate data) {
        String mes = data.getMonth().getDisplayName(TextStyle.FULL, PT_BR);
        String prefixo = data.getDayOfMonth() == 1 ? "No 1º dia" : "Aos " + data.getDayOfMonth() + " dias";
        return prefixo + " do mês de " + mes + " de " + data.getYear();
    }

    /** Nome do mês em português, minúsculo. */
    public static String mes(Month mes) {
        return mes.getDisplayName(TextStyle.FULL, PT_BR);
    }

    /** Junta itens em português: "A", "A e B", "A, B e C". */
    public static String listar(List<String> itens) {
        if (itens.size() <= 1) {
            return String.join("", itens);
        }
        String inicio = String.join(", ", itens.subList(0, itens.size() - 1));
        return inicio + " e " + itens.get(itens.size() - 1);
    }
}
