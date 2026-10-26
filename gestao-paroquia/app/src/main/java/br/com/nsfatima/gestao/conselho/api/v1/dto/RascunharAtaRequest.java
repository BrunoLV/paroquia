package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Abertura de uma ata em rascunho. O status inicial, o redator e o número são sempre definidos pelo
 * servidor, nunca informados pelo cliente.
 */
public record RascunharAtaRequest(
        @NotNull TipoReuniao tipo,
        @NotNull UUID organizacaoConvocanteId,
        UUID organizacaoAssociadaId,
        @NotNull @Valid ConteudoAtaRequest conteudo) {
}
