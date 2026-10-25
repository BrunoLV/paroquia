package br.com.nsfatima.gestao.conselho.application.dto;

import br.com.nsfatima.gestao.conselho.domain.model.ConteudoAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;

import java.util.UUID;

/**
 * Pedido de abertura de uma ata em rascunho. {@code organizacaoAssociadaId} só é informado na reunião
 * conjunta do CPP e do CAEP.
 */
public record RascunharAtaComando(
        TipoReuniao tipo,
        UUID organizacaoConvocanteId,
        UUID organizacaoAssociadaId,
        ConteudoAta conteudo) {
}
