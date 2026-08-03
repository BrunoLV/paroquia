package br.com.nsfatima.gestao.importacao.domain.model;

/**
 * Tipos de entidade suportados pelo fluxo de importação em lote (template, validação e
 * confirmação). Cada valor corresponde a um conjunto próprio de colunas de arquivo, regras de
 * validação por linha e lógica de persistência em {@code ImportacaoService} e
 * {@code ImportacaoFileParser} — adicionar um novo tipo exige atualizar os `switch` de ambas as
 * classes.
 */
public enum TipoEntidadeImportacao {
    /** Vínculo de um usuário já existente a uma organização, com papel organizacional. */
    MEMBRO,
    /** Cadastro de um novo usuário do sistema (conta + dados sacramentais). */
    USUARIO,
    /** Cadastro de uma organização na hierarquia paroquial (comunidade, pastoral, conselho etc.). */
    ORGANIZACAO,
    /** Cadastro de um evento de agenda. */
    EVENTO;

    /**
     * Resolve o código textual recebido do arquivo/requisição (case-insensitive) para o
     * enum correspondente.
     *
     * @param rawValue código informado pelo usuário (ex.: "membro", "MEMBRO")
     * @return o {@link TipoEntidadeImportacao} correspondente, ou {@code null} se o código não
     *     corresponder a nenhum tipo conhecido (tratado pelos chamadores como erro de validação)
     */
    public static TipoEntidadeImportacao fromCode(String rawValue) {
        for (TipoEntidadeImportacao constante : values()) {
            if (constante.name().equalsIgnoreCase(rawValue)) {
                return constante;
            }
        }
        return null;
    }
}
