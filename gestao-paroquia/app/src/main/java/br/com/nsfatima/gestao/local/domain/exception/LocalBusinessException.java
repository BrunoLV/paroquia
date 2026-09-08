package br.com.nsfatima.gestao.local.domain.exception;

import java.util.UUID;

/**
 * Sinaliza violações de regra de negócio do contexto {@code local} (Local não encontrado, ou Local
 * em uso com eventos futuros agendados impedindo inativação ou exclusão lógica).
 */
public class LocalBusinessException extends RuntimeException {

    /**
     * Cria a exceção com uma mensagem de negócio específica.
     *
     * @param message descrição da violação de regra
     */
    public LocalBusinessException(String message) {
        super(message);
    }

    /**
     * Cria a exceção para o caso de um Local não ser encontrado pelo ID informado.
     *
     * @param id ID do local não encontrado
     * @return exceção pronta para ser lançada
     */
    public static LocalBusinessException notFound(UUID id) {
        return new LocalBusinessException("Location not found with ID: " + id);
    }

    /**
     * Cria a exceção para o caso de tentativa de remoção física de um Local que ainda possui
     * Eventos associados — a remoção quebraria o histórico dessas referências.
     *
     * @param id ID do local que está em uso
     * @return exceção pronta para ser lançada
     */
    public static LocalBusinessException inUse(UUID id) {
        return new LocalBusinessException("Cannot delete location " + id + " because it has associated events.");
    }

    /**
     * Cria a exceção para bloqueio de exclusão de local com eventos futuros agendados.
     *
     * @param id ID do local
     * @return exceção pronta para ser lançada
     */
    public static LocalBusinessException hasFutureEventsOnDelete(UUID id) {
        return new LocalBusinessException(
                "Não é permitido excluir o local " + id + " pois existem eventos futuros agendados.");
    }

    /**
     * Cria a exceção para bloqueio de inativação de local com eventos futuros agendados.
     *
     * @param id ID do local
     * @return exceção pronta para ser lançada
     */
    public static LocalBusinessException hasFutureEventsOnDeactivate(UUID id) {
        return new LocalBusinessException(
                "Não é permitido inativar o local " + id + " pois existem eventos futuros agendados.");
    }
}
