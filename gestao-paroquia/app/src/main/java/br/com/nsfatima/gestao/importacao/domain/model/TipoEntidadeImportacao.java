package br.com.nsfatima.gestao.importacao.domain.model;

public enum TipoEntidadeImportacao {
    MEMBRO,
    USUARIO,
    ORGANIZACAO,
    EVENTO;

    public static TipoEntidadeImportacao fromCode(String rawValue) {
        for (TipoEntidadeImportacao constante : values()) {
            if (constante.name().equalsIgnoreCase(rawValue)) {
                return constante;
            }
        }
        return null;
    }
}
