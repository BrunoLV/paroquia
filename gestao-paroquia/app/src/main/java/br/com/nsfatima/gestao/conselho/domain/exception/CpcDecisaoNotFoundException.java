package br.com.nsfatima.gestao.conselho.domain.exception;

import java.util.UUID;

/**
 * Lançada quando uma ata/decisão de conselho (CPC/CPP) referenciada por ID não existe —
 * tipicamente ao tentar homologar/vetar uma decisão com identificador inválido ou já removido.
 */
public class CpcDecisaoNotFoundException extends RuntimeException {

    /**
     * Constrói a exceção com o id da decisão ausente embutido na mensagem.
     *
     * @param id identificador da decisão de conselho que não foi encontrada
     */
    public CpcDecisaoNotFoundException(UUID id) {
        super("Decisão/Ata de conselho não encontrada com ID: " + id);
    }
}
