/**
 * Contexto de calendário paroquial: eventos ({@code EventoEntity}), recorrência, solenidades/ciclo
 * litúrgico, observações de evento, e métricas de agenda (retrabalho, taxa de eventos extras, SLA
 * de despacho pastoral). É o contexto maior e mais conectado do app ("god node" do grafo de
 * dependências) — {@code EventoEntity}/{@code EventoJpaRepository} concentram a maior parte das
 * regras de negócio e são referenciados por praticamente todos os demais contextos
 * (aprovacao, projeto, patrimonio, grupoParoquial, dashboard).
 *
 * <p>Autorização aqui é sempre baseada em papel do ator + escopo de organização
 * ({@code EventoActorContext}/{@code EventoActorContextResolver}), nunca em posse simples do
 * registro. Consultas a {@code EventoEntity} são deliberadamente segregadas em várias interfaces de
 * repositório por responsabilidade (consulta, validação, estatística, recorrência, idempotência,
 * envolvidos) em vez de um único repositório — ver Javadoc de {@code EventoJpaRepository}.
 *
 * <p>Criar/atualizar/cancelar um evento pode desviar para o fluxo de aprovação do contexto
 * {@code aprovacao} quando o ator não tem alçada suficiente ou a mudança é sensível (ver
 * {@code CriarEventoUseCase}/{@code AtualizarEventoUseCase}/{@code CancelarEventoUseCase} e as
 * estratégias em {@code application.usecase.aprovacao.strategy}).
 *
 * <p>Ciclo de vida de status do evento ({@code EventoStatusInput}/{@code EventoStatusResponse}):
 *
 * <pre>{@code
 * stateDiagram-v2
 *     [*] --> RASCUNHO: evento criado ainda em elaboração
 *     [*] --> CONFIRMADO: evento criado já confirmado na agenda
 *     [*] --> ADICIONADO_EXTRA: evento criado fora do provisionamento anual regular
 *                                (ano litúrgico já fechado)
 *     RASCUNHO --> CONFIRMADO
 *     RASCUNHO --> CANCELADO
 *     CONFIRMADO --> CANCELADO
 *     ADICIONADO_EXTRA --> CANCELADO
 *     CANCELADO --> [*]
 * }</pre>
 *
 * O estado de aprovação pendente (quando a mudança foi desviada para o fluxo de aprovação) não é
 * um valor deste enum — é representado por campos paralelos em {@code EventoResponse}
 * ({@code aprovacaoPendenteId}, {@code tipoSolicitacaoPendente} etc.) enquanto o status "oficial"
 * do evento permanece no valor anterior à mudança solicitada. {@code EventoStatusResponse} ainda
 * acrescenta {@code UNKNOWN_LEGACY} para tolerar, sem falhar a leitura, um valor persistido que não
 * corresponda a nenhuma constante conhecida.
 */
package br.com.nsfatima.gestao.calendario;
