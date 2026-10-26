package br.com.nsfatima.gestao.conselho.application.dto;

/** Formato do arquivo exportado, com o tipo de mídia e a extensão que a resposta HTTP precisa. */
public enum FormatoExportacaoAta {
    PDF("application/pdf", "pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");

    private final String tipoMidia;
    private final String extensao;

    FormatoExportacaoAta(String tipoMidia, String extensao) {
        this.tipoMidia = tipoMidia;
        this.extensao = extensao;
    }

    public String getTipoMidia() {
        return tipoMidia;
    }

    public String getExtensao() {
        return extensao;
    }
}
