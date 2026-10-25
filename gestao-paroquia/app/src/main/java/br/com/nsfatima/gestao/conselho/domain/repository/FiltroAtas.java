package br.com.nsfatima.gestao.conselho.domain.repository;

import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Critérios de listagem de atas. O escopo de leitura faz parte do filtro e é aplicado na consulta: quando
 * {@code irrestrito} é falso, só entram atas cuja organização convocante ou associada esteja em
 * {@code organizacoesPermitidas}. Conjuntos vazios e valores nulos significam "sem restrição" nos demais critérios.
 */
public record FiltroAtas(
        boolean irrestrito,
        Set<UUID> organizacoesPermitidas,
        UUID organizacaoId,
        TipoReuniao tipo,
        Set<StatusAta> statuses,
        LocalDate de,
        LocalDate ate) {

    public FiltroAtas {
        organizacoesPermitidas = organizacoesPermitidas == null ? Set.of() : Set.copyOf(organizacoesPermitidas);
        statuses = statuses == null ? Set.of() : Set.copyOf(statuses);
    }

    /** Indica se o escopo do ator não alcança nenhuma organização, o que dispensa consultar o banco. */
    public boolean semAcesso() {
        return !irrestrito && organizacoesPermitidas.isEmpty();
    }
}
