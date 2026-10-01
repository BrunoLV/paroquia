package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.StatusAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Linha de listagem: identifica o assento pelo sujeito principal sem expor o teor completo. Na consulta por
 * vínculo, {@code participanteId} e {@code papelVinculado} indicam a posição da pessoa consultada no termo.
 */
public record AssentoResumoResponse(
        UUID id,
        TipoSacramento tipo,
        StatusAssento status,
        Visibilidade visibilidade,
        String livro,
        String folha,
        String termo,
        LocalDateTime dataCelebracao,
        String sujeito,
        UUID participanteId,
        String papelVinculado) {

    public static AssentoResumoResponse de(AssentoSacramental a) {
        return de(a, null);
    }

    /**
     * Resumo do assento destacando a posição de uma pessoa vinculada.
     *
     * @param a assento
     * @param pessoaId fiel ou clérigo vinculado, ou {@code null} fora da consulta por vínculo
     * @return resumo com o participante correspondente à pessoa, quando houver
     */
    public static AssentoResumoResponse de(AssentoSacramental a, UUID pessoaId) {
        var vinculado = a.getTeor().participantes().stream()
                .filter(p -> pessoaId != null && (pessoaId.equals(p.fielId()) || pessoaId.equals(p.clerigoId())))
                .findFirst();
        String sujeito = a.getTeor().participantes().stream()
                .filter(p -> sujeitoPrincipal(p.papel()))
                .map(p -> p.nomeHistorico())
                .findFirst()
                .orElse(null);
        return new AssentoResumoResponse(a.getId().value(), a.getTipo(), a.getStatus(), a.getVisibilidade(),
                a.getTeor().referenciaLivro().livro(), a.getTeor().referenciaLivro().folha(),
                a.getTeor().referenciaLivro().termo(), a.getTeor().dataCelebracao(), sujeito,
                vinculado.map(p -> p.id()).orElse(null), vinculado.map(p -> p.papel().name()).orElse(null));
    }

    private static boolean sujeitoPrincipal(PapelParticipante papel) {
        return papel == PapelParticipante.BATIZANDO || papel == PapelParticipante.CRISMANDO
                || papel == PapelParticipante.ORDENADO || papel == PapelParticipante.CONJUGE;
    }
}
