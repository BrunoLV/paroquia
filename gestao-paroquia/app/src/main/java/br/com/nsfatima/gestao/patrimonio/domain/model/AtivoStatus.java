package br.com.nsfatima.gestao.patrimonio.domain.model;

/**
 * Status corrente de um ativo patrimonial no inventário.
 *
 * <ul>
 *   <li>{@link #DISPONIVEL} — apto para uso imediato ou para agendamento de reserva/manutenção.</li>
 *   <li>{@link #RESERVADO} — há uma reserva confirmada ou em custódia cobrindo a data corrente
 *   (dirigido pelo módulo de Reservas; escrito de forma assíncrona por evento/job).</li>
 *   <li>{@link #EM_MANUTENCAO} — fisicamente indisponível por conserto/revisão; tem precedência
 *   sobre {@link #RESERVADO}.</li>
 * </ul>
 *
 * Os estados {@code ALOCADO} e {@code BAIXADO} foram descontinuados: o uso temporário é sempre uma
 * reserva e o descarte patrimonial é feito por exclusão lógica.
 */
public enum AtivoStatus {
    DISPONIVEL,
    RESERVADO,
    EM_MANUTENCAO
}
