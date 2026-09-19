package br.com.nsfatima.gestao.local.domain.exception;

import java.util.UUID;

/**
 * Sinaliza violações de regra de negócio do contexto {@code local} (Local não encontrado, ou Local
 * em uso com eventos futuros agendados impedindo inativação ou exclusão lógica).
 */
public class LocalNegocioException extends RuntimeException {

    /**
     * Cria a exceção com uma mensagem de negócio específica.
     *
     * @param message descrição da violação de regra
     */
    public LocalNegocioException(String message) {
        super(message);
    }

    /**
     * Cria a exceção para o caso de um Local não ser encontrado pelo ID informado.
     *
     * @param id ID do local não encontrado
     * @return exceção pronta para ser lançada
     */
    public static LocalNegocioException notFound(UUID id) {
        return new LocalNegocioException("Local não encontrado com ID: " + id);
    }

    /**
     * Cria a exceção para o caso de tentativa de remoção física de um Local que ainda possui
     * Eventos associados — a remoção quebraria o histórico dessas referências.
     *
     * @param id ID do local que está em uso
     * @return exceção pronta para ser lançada
     */
    public static LocalNegocioException inUse(UUID id) {
        return new LocalNegocioException("Não é permitido excluir o local " + id + " pois possui eventos associados.");
    }

    /**
     * Cria a exceção para bloqueio de exclusão de local com eventos futuros agendados.
     *
     * @param id ID do local
     * @return exceção pronta para ser lançada
     */
    public static LocalNegocioException hasFutureEventsOnDelete(UUID id) {
        return new LocalNegocioException(
                "Não é permitido excluir o local " + id + " pois existem eventos futuros agendados.");
    }

    /**
     * Cria a exceção para bloqueio de inativação de local com eventos futuros agendados.
     *
     * @param id ID do local
     * @return exceção pronta para ser lançada
     */
    public static LocalNegocioException hasFutureEventsOnDeactivate(UUID id) {
        return new LocalNegocioException(
                "Não é permitido inativar o local " + id + " pois existem eventos futuros agendados.");
    }
}
