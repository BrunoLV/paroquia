package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;

import java.util.List;

/**
 * Item da ordem do dia: o assunto tratado, um relato resumido da discussão e as deliberações dele
 * decorrentes. As deliberações pertencem ao item para que a ata, estruturada ou narrativa, mantenha a
 * ligação entre assunto e conclusão.
 */
public record ItemPauta(int ordem, String titulo, String relato, List<Deliberacao> deliberacoes) {

    private static final int TITULO_MAX = 160;
    private static final int RELATO_MAX = 4000;

    public ItemPauta {
        if (ordem < 1) {
            throw new AtaInvalidaException("A ordem do item de pauta começa em 1.");
        }
        if (titulo == null || titulo.isBlank()) {
            throw new AtaInvalidaException("O título do item de pauta é obrigatório.");
        }
        titulo = titulo.trim();
        if (titulo.length() > TITULO_MAX) {
            throw new AtaInvalidaException("O título do item de pauta excede " + TITULO_MAX + " caracteres.");
        }
        relato = relato == null || relato.isBlank() ? null : relato.trim();
        if (relato != null && relato.length() > RELATO_MAX) {
            throw new AtaInvalidaException("O relato do item de pauta excede " + RELATO_MAX + " caracteres.");
        }
        deliberacoes = deliberacoes == null ? List.of() : List.copyOf(deliberacoes);
        for (int i = 0; i < deliberacoes.size(); i++) {
            if (deliberacoes.get(i).ordem() != i + 1) {
                throw new AtaInvalidaException("As deliberações do item seguem a sequência a partir de 1.");
            }
        }
    }
}
