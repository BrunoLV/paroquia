package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import br.com.nsfatima.gestao.sacramento.domain.model.AssentoSacramental;
import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.sacramento.domain.model.StatusAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;

import java.time.LocalDateTime;
import java.util.UUID;

/** Linha de listagem: identifica o assento pelo sujeito principal sem expor o teor completo. */
public record AssentoResumoResponse(
        UUID id,
        TipoSacramento tipo,
        StatusAssento status,
        Visibilidade visibilidade,
        String livro,
        String folha,
        String termo,
        LocalDateTime dataCelebracao,
        String sujeito) {

    public static AssentoResumoResponse de(AssentoSacramental a) {
        String sujeito = a.getTeor().participantes().stream()
                .filter(p -> sujeitoPrincipal(p.papel()))
                .map(p -> p.nomeHistorico())
                .findFirst()
                .orElse(null);
        return new AssentoResumoResponse(a.getId().value(), a.getTipo(), a.getStatus(), a.getVisibilidade(),
                a.getTeor().referenciaLivro().livro(), a.getTeor().referenciaLivro().folha(),
                a.getTeor().referenciaLivro().termo(), a.getTeor().dataCelebracao(), sujeito);
    }

    private static boolean sujeitoPrincipal(PapelParticipante papel) {
        return papel == PapelParticipante.BATIZANDO || papel == PapelParticipante.CRISMANDO
                || papel == PapelParticipante.ORDENADO || papel == PapelParticipante.CONJUGE;
    }
}
