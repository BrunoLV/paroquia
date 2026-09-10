package br.com.nsfatima.gestao.patrimonio.application.port.out;

import br.com.nsfatima.gestao.patrimonio.domain.model.ReservaImpeditiva;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Porta de saída (ACL) do contexto de patrimônio para consultar o estado de reservas de um ativo
 * no módulo de Reservas, sem depender do agregado {@code Reserva}. Implementada por um adaptador em
 * {@code infrastructure/acl} que delega ao serviço público do módulo de reserva.
 */
public interface ConsultaReservaDeAtivoPort {

    /** Há reserva confirmada ou em custódia cobrindo o instante atual? Define o status {@code RESERVADO}. */
    boolean temReservaAtivaAgora(UUID ativoId);

    /**
     * Reservas {@code PENDENTE} ou {@code CONFIRMADA} que se sobrepõem ao intervalo — impedem o
     * envio do ativo para manutenção nesse período.
     */
    List<ReservaImpeditiva> reservasImpeditivas(UUID ativoId, Instant inicio, Instant fim);

    /** Há reserva {@code PENDENTE}/{@code CONFIRMADA} com início no futuro? Impede a exclusão do ativo. */
    boolean temReservaFuturaImpeditiva(UUID ativoId);
}
