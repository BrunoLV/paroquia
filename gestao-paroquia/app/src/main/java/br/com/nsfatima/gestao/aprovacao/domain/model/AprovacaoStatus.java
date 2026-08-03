package br.com.nsfatima.gestao.aprovacao.domain.model;

import br.com.nsfatima.gestao.calendario.api.v1.dto.support.EnumRequestNormalizer;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Estados do ciclo de vida de uma solicitação de aprovação. Toda solicitação nasce PENDENTE (criada por
 * {@code CreateSolicitacaoAprovacaoUseCase} quando uma ação de negócio exige aprovação de um papel superior)
 * e é decidida uma única vez por {@code DecideSolicitacaoAprovacaoUseCase}:
 * <ul>
 *   <li>{@link #PENDENTE} — aguardando decisão de um aprovador autorizado; estado inicial e único a partir do
 *   qual uma decisão pode ser tomada ({@code AprovacaoJaDecididaException} é lançada ao tentar decidir uma
 *   solicitação que já saiu deste estado).</li>
 *   <li>{@link #APROVADA} — decisão positiva e ação de negócio associada (via
 *   {@code EstrategiaExecucaoAprovacao}) executada com sucesso.</li>
 *   <li>{@link #REPROVADA} — decisão negativa; nenhuma ação de negócio é efetivada.</li>
 *   <li>{@link #FALHA_EXECUCAO} — a solicitação foi aprovada, porém a execução da ação de negócio associada
 *   falhou (ex.: conflito de agenda detectado apenas no momento da execução); a decisão em si foi registrada,
 *   mas o efeito esperado não ocorreu e requer atenção.</li>
 * </ul>
 */
public enum AprovacaoStatus {
    PENDENTE,
    APROVADA,
    REPROVADA,
    FALHA_EXECUCAO;

    /**
     * Normaliza um valor textual (ex.: vindo de request JSON, possivelmente com variações de caixa) para o
     * enum correspondente.
     *
     * @param rawValue valor bruto recebido na requisição
     * @return status normalizado
     */
    @JsonCreator
    public static AprovacaoStatus fromJson(String rawValue) {
        return EnumRequestNormalizer.normalize(rawValue, AprovacaoStatus.class);
    }
}
