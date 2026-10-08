package br.com.nsfatima.gestao.membro.domain.model;

import java.time.LocalDate;

/**
 * Registro de idoneidade pastoral de um agente (fiel vinculado a uma organização): formação doutrinária
 * básica, vivência sacramental atestada, termo de compromisso de proteção de menores e vulneráveis e o envio
 * (missão) conferido pelo Pároco. Guarda apenas datas e quem enviou, nunca o conteúdo dos documentos (LGPD).
 *
 * @param formacaoConcluidaEm data de conclusão da formação doutrinária básica
 * @param vivenciaSacramentalAtestadaEm data em que o Pároco atestou a vivência sacramental condizente com a função
 * @param termoProtecaoMenoresEm data de assinatura do termo de proteção de menores e vulneráveis
 * @param termoProtecaoMenoresValidoAte último dia de validade do termo
 * @param enviadoEm data do envio (missão) do agente
 * @param enviadoPor papel/identificador de quem conferiu o envio
 */
public record IdoneidadeAgente(
        LocalDate formacaoConcluidaEm,
        LocalDate vivenciaSacramentalAtestadaEm,
        LocalDate termoProtecaoMenoresEm,
        LocalDate termoProtecaoMenoresValidoAte,
        LocalDate enviadoEm,
        String enviadoPor) {

    public IdoneidadeAgente {
        LocalDate hoje = LocalDate.now();
        naoFutura(formacaoConcluidaEm, hoje, "formação");
        naoFutura(vivenciaSacramentalAtestadaEm, hoje, "atestado de vivência sacramental");
        naoFutura(termoProtecaoMenoresEm, hoje, "termo de proteção de menores");
        naoFutura(enviadoEm, hoje, "envio");
        if (termoProtecaoMenoresValidoAte != null
                && (termoProtecaoMenoresEm == null || !termoProtecaoMenoresValidoAte.isAfter(termoProtecaoMenoresEm))) {
            throw new IllegalArgumentException(
                    "A validade do termo de proteção de menores deve ser posterior à data de assinatura.");
        }
        enviadoPor = enviadoPor == null || enviadoPor.isBlank() ? null : enviadoPor.trim();
    }

    /**
     * Registro vazio, para agentes ainda sem nenhum requisito atestado.
     *
     * @return idoneidade sem nenhuma data preenchida
     */
    public static IdoneidadeAgente vazia() {
        return new IdoneidadeAgente(null, null, null, null, null, null);
    }

    /**
     * Indica se o termo de proteção de menores está em vigor na data informada. Sem validade informada o termo
     * é considerado vigente a partir da assinatura.
     *
     * @param data data de referência
     * @return {@code true} se há termo assinado e não vencido
     */
    public boolean termoVigenteEm(LocalDate data) {
        return termoProtecaoMenoresEm != null
                && (termoProtecaoMenoresValidoAte == null || !termoProtecaoMenoresValidoAte.isBefore(data));
    }

    private static void naoFutura(LocalDate data, LocalDate hoje, String descricao) {
        if (data != null && data.isAfter(hoje)) {
            throw new IllegalArgumentException("A data de " + descricao + " não pode ser futura.");
        }
    }
}
