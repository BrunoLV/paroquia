package br.com.nsfatima.gestao.reserva.application.port.out;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Porta de saída (ACL) do módulo de Reservas para consultar janelas de manutenção de um ativo no
 * contexto de patrimônio, usadas para bloquear o calendário e recusar reservas sobrepostas.
 */
public interface ConsultaManutencaoAtivoPort {

    /** Janelas [inicio, fim) de manutenção do ativo que se sobrepõem ao intervalo consultado. */
    List<JanelaIndisponivel> janelasDeManutencao(UUID ativoId, Instant inicio, Instant fim);

    /** Intervalo de indisponibilidade por manutenção. */
    record JanelaIndisponivel(Instant inicio, Instant fim) {
    }
}
