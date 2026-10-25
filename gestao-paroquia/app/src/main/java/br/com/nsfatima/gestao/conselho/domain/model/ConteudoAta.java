package br.com.nsfatima.gestao.conselho.domain.model;

import br.com.nsfatima.gestao.conselho.domain.exception.AtaInvalidaException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Parte editável da ata enquanto rascunho. Agrupa o que o redator escreve para que a edição seja uma
 * substituição inteira e validada ({@code AtaReuniao.redigir}), nunca uma sequência de setters. As regras
 * que dependem do tipo de reunião (modo de presença, votação) são conferidas pelo agregado, que conhece o tipo.
 */
public record ConteudoAta(
        String titulo,
        LocalDate dataReuniao,
        String local,
        CaraterReuniao carater,
        ModalidadeReuniao modalidade,
        PresencaReuniao presenca,
        AtaAnterior ataAnterior,
        List<ItemPauta> itens) {

    private static final int TITULO_MAX = 160;
    private static final int LOCAL_MAX = 200;

    public ConteudoAta {
        titulo = exigirTexto(titulo, TITULO_MAX, "título da ata");
        Objects.requireNonNull(dataReuniao, "A data da reunião é obrigatória.");
        local = exigirTexto(local, LOCAL_MAX, "local da reunião");
        Objects.requireNonNull(carater, "O caráter da reunião é obrigatório.");
        Objects.requireNonNull(modalidade, "A modalidade da reunião é obrigatória.");
        presenca = presenca == null ? PresencaReuniao.vazia() : presenca;
        ataAnterior = ataAnterior == null ? AtaAnterior.nenhuma() : ataAnterior;
        itens = itens == null ? List.of() : List.copyOf(itens);
        for (int i = 0; i < itens.size(); i++) {
            if (itens.get(i).ordem() != i + 1) {
                throw new AtaInvalidaException("Os itens de pauta devem ser numerados em sequência a partir de 1.");
            }
        }
    }

    private static String exigirTexto(String valor, int max, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new AtaInvalidaException("O " + campo + " é obrigatório.");
        }
        String limpo = valor.trim();
        if (limpo.length() > max) {
            throw new AtaInvalidaException("O " + campo + " excede " + max + " caracteres.");
        }
        return limpo;
    }
}
