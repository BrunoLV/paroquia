package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Representa uma organização envolvida em um evento e o papel que ela desempenha (ex: organizadora,
 * apoiadora, convidada), usado tanto na criação quanto na atualização da lista de envolvidos de um
 * evento.
 */
public record EventoEnvolvidoInput(
        @NotNull UUID grupoParoquialId,
        @NotNull PapelEnvolvido papel) {
}
