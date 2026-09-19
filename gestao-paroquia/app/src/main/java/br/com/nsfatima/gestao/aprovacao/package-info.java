/**
 * Fluxo de aprovação transversal: quando um ator sem alçada suficiente tenta executar uma ação de
 * negócio sensível (criar/atualizar/cancelar evento, cancelar projeto, alocar ativo patrimonial), a
 * ação não é aplicada de imediato — em vez disso, uma {@code AprovacaoEntity} é criada em estado
 * {@code PENDENTE} e a execução real é adiada até que um ator com alçada superior decida, via
 * {@code DecidirSolicitacaoAprovacaoUseCase}.
 *
 * <p>Cada bounded context que precisa desse desvio (calendario, projeto, patrimonio) implementa a
 * interface {@code EstrategiaExecucaoAprovacao} (padrão Strategy) para plugar sua própria lógica de
 * execução quando a solicitação é aprovada, sem que este contexto precise conhecer os detalhes de
 * cada tipo de ação — o payload da ação pendente é serializado/restaurado via
 * {@code AprovacaoAcaoPayloadMapper}.
 *
 * <p>Ciclo de vida de uma solicitação ({@code AprovacaoStatus}):
 *
 * <pre>{@code
 * stateDiagram-v2
 *     [*] --> PENDENTE: ação sensível solicitada por ator sem alçada
 *     PENDENTE --> APROVADA: aprovador decide aprovar e execução da ação de negócio sucede
 *     PENDENTE --> REPROVADA: aprovador decide reprovar (nenhuma ação de negócio é efetivada)
 *     PENDENTE --> FALHA_EXECUCAO: aprovador decide aprovar, mas a execução da ação falha
 *                                   (ex.: conflito de agenda detectado só na execução)
 *     APROVADA --> [*]
 *     REPROVADA --> [*]
 *     FALHA_EXECUCAO --> [*]
 * }</pre>
 *
 * PENDENTE é o único estado a partir do qual uma decisão pode ser tomada — tentar decidir uma
 * solicitação já decidida lança {@code AprovacaoJaDecididaException}.
 */
package br.com.nsfatima.gestao.aprovacao;
