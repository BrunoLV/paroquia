package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.ResultadoDeliberacao;

import java.util.List;

/** Deliberação de um item de pauta. */
public record DeliberacaoResponse(
        int ordem,
        String texto,
        ResultadoDeliberacao resultado,
        VotacaoRequest votacao,
        List<ConflitoInteresseResponse> conflitos) {
}
