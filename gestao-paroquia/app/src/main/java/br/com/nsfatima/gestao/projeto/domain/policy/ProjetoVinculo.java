package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;

import java.time.Instant;
import java.util.Objects;

/** Snapshot imutável suficiente para validar um vínculo sem vazar JPA ao domínio. */
public record ProjetoVinculo(String nome, ProjetoStatus status, Instant inicio, Instant fim) {

    public ProjetoVinculo {
        Objects.requireNonNull(nome, "Nome do projeto é obrigatório");
        Objects.requireNonNull(status, "Status do projeto é obrigatório");
    }
}
