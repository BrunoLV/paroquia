package br.com.nsfatima.gestao.conselho.application.dto;

import java.util.List;

/**
 * Documento pronto para ser desenhado por um exportador: cabeçalho institucional, título, os blocos de conteúdo
 * e a linha de emissão do rodapé. {@code minuta} liga a marca "MINUTA — SEM EFEITO" quando alguma ata ainda não
 * foi selada.
 */
public record DocumentoReuniao(
        CabecalhoParoquia cabecalho,
        String titulo,
        boolean minuta,
        String emissao,
        List<BlocoDocumento> blocos) {

    public DocumentoReuniao {
        blocos = List.copyOf(blocos);
    }
}
