package br.com.nsfatima.gestao.sacramento.domain.model;

/**
 * Localização do termo no livro oficial. Todos os campos são obrigatórios apenas na homologação;
 * em rascunho podem estar em branco enquanto a escrituração é preparada.
 */
public record ReferenciaLivro(String livro, String folha, String termo) {

    public static ReferenciaLivro vazia() {
        return new ReferenciaLivro(null, null, null);
    }

    public boolean completa() {
        return preenchido(livro) && preenchido(folha) && preenchido(termo);
    }

    private static boolean preenchido(String v) {
        return v != null && !v.isBlank();
    }
}
