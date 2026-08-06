package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;

import java.util.List;
import java.util.UUID;

/**
 * Lista completa e atual das organizações envolvidas em um evento, com o papel de cada uma,
 * retornada após consultas ou operações de atualização/limpeza dos envolvidos.
 */
public record EventoEnvolvidosResponse(
        UUID eventoId,
        List<EventoEnvolvidoOutput> envolvidos) {

    /**
     * Representa uma organização envolvida e o papel que ela desempenha no evento.
     */
    public record EventoEnvolvidoOutput(
            UUID grupoParoquialId,
            PapelEnvolvido papel) {
    }
}
