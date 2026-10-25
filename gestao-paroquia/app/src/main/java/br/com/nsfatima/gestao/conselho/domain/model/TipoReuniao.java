package br.com.nsfatima.gestao.conselho.domain.model;

/**
 * Catálogo dos tipos de reunião registráveis em ata na paróquia e nas comunidades. Os órgãos colegiados
 * (CPP, CPC, CAEP e a reunião conjunta) têm base explícita no Documento 100 da CNBB (nn. 291-294) e nos
 * cânones 536 e 537; os demais tipos refletem a prática pastoral e não são definidos formalmente pela CNBB.
 *
 * <p>O que cada tipo exige (convocante aceito, homologação, quórum, presença, votação e redator) não vive
 * aqui: pertence a {@code RegrasTipoReuniaoPolicy}, para que a variação por tipo não se espalhe em
 * condicionais.</p>
 */
public enum TipoReuniao {
    REUNIAO_CPP("Reunião do Conselho Pastoral Paroquial (CPP)",
            "Ata da Reunião do Conselho Pastoral Paroquial"),
    REUNIAO_CPC("Reunião do Conselho Pastoral Comunitário (CPC)",
            "Ata da Reunião do Conselho Pastoral Comunitário"),
    REUNIAO_CAEP("Reunião do Conselho de Assuntos Econômicos (CAEP)",
            "Ata da Reunião do Conselho de Assuntos Econômicos"),
    REUNIAO_CONJUNTA_CPP_CAEP("Reunião Conjunta do CPP e do CAEP",
            "Ata da Reunião Conjunta do Conselho Pastoral Paroquial e do Conselho de Assuntos Econômicos"),
    REUNIAO_PASTORAL("Reunião de Pastoral (conselho e coordenadores)",
            "Ata da Reunião de Pastoral"),
    REUNIAO_COORDENADORES("Reunião de Coordenadores",
            "Ata da Reunião de Coordenadores"),
    ASSEMBLEIA_PAROQUIAL("Assembleia Paroquial",
            "Ata da Assembleia Paroquial"),
    ASSEMBLEIA_COMUNITARIA("Assembleia Comunitária",
            "Ata da Assembleia Comunitária"),
    REUNIAO_ORGANISMO("Reunião de Pastoral, Movimento ou Serviço",
            "Ata da Reunião do Organismo"),
    PLANEJAMENTO_PASTORAL("Planejamento Pastoral",
            "Ata do Planejamento Pastoral"),
    COMISSAO_ESPECIAL("Comissão Especial",
            "Ata da Comissão Especial"),
    REUNIAO_ANIMADORES("Reunião de Animadores de Comunidades",
            "Ata da Reunião de Animadores de Comunidades");

    private final String rotulo;
    private final String tituloDaAta;

    TipoReuniao(String rotulo, String tituloDaAta) {
        this.rotulo = rotulo;
        this.tituloDaAta = tituloDaAta;
    }

    /** Nome do tipo apresentado ao usuário ao escolher a reunião. */
    public String getRotulo() {
        return rotulo;
    }

    /** Título impresso no cabeçalho do documento exportado. */
    public String getTituloDaAta() {
        return tituloDaAta;
    }
}
