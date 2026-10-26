package br.com.nsfatima.gestao.conselho.application.port.out;

import br.com.nsfatima.gestao.conselho.application.dto.DocumentoReuniao;
import br.com.nsfatima.gestao.conselho.application.dto.FormatoExportacaoAta;

/**
 * Porta de saída de exportação (Strategy): cada adaptador desenha o {@link DocumentoReuniao} em um formato.
 * O caso de uso escolhe o adaptador por {@link #suporta(FormatoExportacaoAta)}.
 */
public interface ExportadorAta {

    /**
     * Indica se o adaptador gera o formato.
     *
     * @param formato formato pedido
     * @return {@code true} se este adaptador o trata
     */
    boolean suporta(FormatoExportacaoAta formato);

    /**
     * Desenha o documento.
     *
     * @param documento conteúdo neutro a desenhar
     * @return bytes do arquivo gerado
     */
    byte[] exportar(DocumentoReuniao documento);
}
