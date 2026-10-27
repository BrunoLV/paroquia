package br.com.nsfatima.gestao.reserva.application.port.out;

import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de saída (ACL) do módulo de Reservas para o cadastro dos itens reservados (locais e ativos
 * patrimoniais). Responde se o item existe, está em uso e é elegível a reserva e, no mesmo ato, bloqueia o item
 * até o fim da transação corrente.
 *
 * <p>O bloqueio é o ponto de serialização compartilhado: dois pedidos para o mesmo item, ou um pedido e o envio
 * do ativo para manutenção, passam um de cada vez por ele, o que torna a consulta de conflitos posterior
 * confiável sob concorrência.</p>
 */
public interface ItemReservavelPort {

    /**
     * Carrega e bloqueia o item. Exige transação ativa.
     *
     * @return o item, ou vazio se não existir
     */
    Optional<ItemReservavel> bloquear(TipoItemReserva tipoItem, UUID itemId);

    /**
     * Visão do item na ótica da reserva.
     *
     * @param ativo      item em uso (local ativo e não excluído; ativo patrimonial não excluído)
     * @param reservavel item elegível a reserva (sempre verdadeiro para locais)
     */
    record ItemReservavel(UUID id, String nome, boolean ativo, boolean reservavel) {
    }
}
