/**
 * Contexto de patrimônio: gestão de ativos patrimoniais da paróquia ({@code AtivoPatrimonialEntity}),
 * sua alocação/empréstimo a eventos por período ({@code AtivoAlocacaoEntity}), manutenção, e
 * restrição de posse por organização ({@code AtivoGrupoParoquialPolicy}).
 *
 * <p>Segue o mesmo padrão de desvio para aprovação do contexto {@code aprovacao}: uma alocação
 * solicitada por um ator sem alçada suficiente vira uma solicitação pendente
 * ({@code AlocacaoAtivoExecutionStrategy}) em vez de ser efetivada de imediato. Alocações têm
 * conflito verificado por período ({@code AtivoAlocacaoConflitoPolicy}), e um job agendado
 * (`AtivoAlocacaoAgendaJob`) trata alocações em atraso.
 */
package br.com.nsfatima.gestao.patrimonio;
