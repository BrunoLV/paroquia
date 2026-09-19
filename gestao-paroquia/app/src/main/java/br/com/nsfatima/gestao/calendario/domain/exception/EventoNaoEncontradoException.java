package br.com.nsfatima.gestao.calendario.domain.exception;

import java.util.UUID;

/**
 * Lançada quando um evento de calendário referenciado por identificador não é
 * encontrado — usada em casos de uso de cancelamento, atualização, recorrência e
 * consulta, e também por outros contextos (ex.: patrimônio) que referenciam um
 * evento por ID.
 */
public class EventoNaoEncontradoException extends RuntimeException {

    /**
     * Cria a exceção para o identificador de evento não encontrado.
     *
     * @param eventoId identificador do evento não encontrado
     */
    public EventoNaoEncontradoException(UUID eventoId) {
        super("Evento não encontrado: " + eventoId);
    }
}
