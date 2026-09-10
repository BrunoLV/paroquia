/**
 * Contexto de patrimônio: gestão do ciclo de vida dos bens patrimoniais móveis da paróquia.
 *
 * <p>Agregados de domínio puros: {@code AtivoPatrimonial} (imutável exceto {@code descricao};
 * posse fixa por unidade territorial, sem transferência) e {@code AtivoManutencao} (período de
 * indisponibilidade técnica). O status do ativo é {@code DISPONIVEL}, {@code RESERVADO} ou
 * {@code EM_MANUTENCAO}; o descarte é exclusão lógica.</p>
 *
 * <p>O uso temporário do bem é sempre uma reserva do módulo {@code reserva}. A integração é
 * bidirecional via ACL: patrimônio consulta reservas ativas/impeditivas
 * ({@code ConsultaReservaDeAtivoPort}) e reserva consulta janelas de manutenção
 * ({@code ConsultaManutencaoAtivoService}). O status {@code RESERVADO} é escrito de forma
 * assíncrona por um listener de eventos de reserva mais um job Quartz de varredura.</p>
 */
package br.com.nsfatima.gestao.patrimonio;
