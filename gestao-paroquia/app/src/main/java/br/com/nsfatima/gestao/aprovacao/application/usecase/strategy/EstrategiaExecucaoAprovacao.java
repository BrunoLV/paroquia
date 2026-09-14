package br.com.nsfatima.gestao.aprovacao.application.usecase.strategy;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;

/**
 * Estratégia (padrão Strategy) que encapsula, por tipo de solicitação, a ação de negócio a ser efetivamente
 * executada quando uma aprovação é decidida como APROVADA. Permite que diferentes contextos limitados
 * (calendario, projeto, patrimonio) registrem sua própria lógica de execução sem que
 * {@code DecideSolicitacaoAprovacaoUseCase} precise conhecer os detalhes de cada tipo de solicitação — o use
 * case apenas seleciona, dentre as implementações injetadas, a que responde {@code true} em
 * {@link #supports(TipoSolicitacaoInput)}.
 */
public interface EstrategiaExecucaoAprovacao {

    /**
     * Indica se esta estratégia é responsável por executar a ação associada ao tipo de solicitação informado.
     *
     * @param tipo tipo da solicitação de aprovação
     * @return {@code true} se esta estratégia trata o tipo informado
     */
    boolean supports(TipoSolicitacaoInput tipo);

    /**
     * Executa a ação de negócio referente à solicitação aprovada (ex.: efetivar a criação/atualização/cancelamento
     * de um evento). Lançar uma exceção em tempo de execução sinaliza falha de execução, levando o use case
     * chamador a marcar a aprovação como FALHA_EXECUCAO (aprovada, porém não efetivada).
     *
     * @param aprovacao   aprovação já marcada como decidida, contendo o payload da ação pendente
     * @param actorContext contexto de autorização do ator que tomou a decisão
     * @return detalhes do resultado da execução, usados na resposta da API e nas notificações
     */
    AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao, EventoActorContext actorContext);

    /**
     * Consulta o status atual do recurso afetado pela solicitação (ex.: status do evento), usado para reportar
     * o estado do recurso após uma rejeição.
     *
     * @param aprovacao aprovação associada ao recurso
     * @return representação textual do status atual do recurso
     */
    String fetchResourceStatus(AprovacaoEntity aprovacao);

    /**
     * Executado quando a solicitação é REPROVADA, permitindo que a estratégia limpe qualquer estado pendente
     * associado ao recurso (ex.: reverter uma reserva provisória). Sem efeito por padrão.
     *
     * @param aprovacao aprovação que foi reprovada
     */
    default void onRejected(AprovacaoEntity aprovacao) {
        // Sem efeito por padrão; estratégias cujo recurso pendente precise de limpeza na rejeição podem sobrescrever isto.
    }
}
