package br.com.nsfatima.gestao.aprovacao.domain.model;

import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoExecucaoFalhouException;
import br.com.nsfatima.gestao.aprovacao.domain.exception.AprovacaoJaDecididaException;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raiz de Solicitação de Aprovação.
 *
 * <p>Concentra a máquina de estados {@code PENDENTE -> APROVADA | REPROVADA} e o pós-processamento
 * da execução ({@code APROVADA -> FALHA_EXECUCAO} ou registro de conclusão) com suporte a categorização
 * de falhas (negócio/cadastral vs. sistêmica/aplicação) e reexecução controlada.</p>
 *
 * <p>Invariantes:</p>
 * <ul>
 *   <li>Só uma solicitação {@code PENDENTE} pode ser decidida; decidir de novo lança
 *   {@link AprovacaoJaDecididaException}.</li>
 *   <li>Uma solicitação cuja ação já deveria ter começado (evento no passado) não pode ser decidida
 *   — {@link AprovacaoExecucaoFalhouException}.</li>
 *   <li>A conclusão/falha de execução só se registra sobre uma solicitação {@code APROVADA}.</li>
 *   <li>A reexecução só é admitida para solicitações em {@code FALHA_EXECUCAO} de natureza {@code SISTEMICA_APLICACAO}.
 *   Falhas cadastrais/de negócio são imutáveis e exigem novo cadastro com parâmetros corrigidos.</li>
 * </ul>
 */
public class Aprovacao {

    private final UUID id;
    private final UUID eventoId;
    private final String tipoSolicitacao;
    private final Instant criadoEmUtc;
    private final String solicitanteId;
    private final String actionPayloadJson;
    private final String correlationId;

    private String aprovadorPapel;
    private AprovacaoStatus status;
    private Instant decididoEmUtc;
    private Instant executadoEmUtc;
    private String aprovadorId;
    private String decisionObservacao;
    private String mensagemErroExecucao;
    private TipoFalhaAprovacao tipoFalhaExecucao;
    private String detalheErroExecucao;
    private final UUID aprovacaoPaiId;
    private final String conflitosJson;
    private final Instant dataOcorrenciaUtc;
    private final String justificativa;

    public Aprovacao(
            UUID id,
            UUID eventoId,
            String tipoSolicitacao,
            String aprovadorPapel,
            AprovacaoStatus status,
            Instant criadoEmUtc,
            Instant decididoEmUtc,
            Instant executadoEmUtc,
            String solicitanteId,
            String aprovadorId,
            String decisionObservacao,
            String actionPayloadJson,
            String correlationId,
            String mensagemErroExecucao,
            TipoFalhaAprovacao tipoFalhaExecucao,
            String detalheErroExecucao,
            UUID aprovacaoPaiId,
            String conflitosJson,
            Instant dataOcorrenciaUtc,
            String justificativa) {
        this.id = Objects.requireNonNull(id, "ID da aprovação é obrigatório.");
        this.eventoId = eventoId;
        this.tipoSolicitacao = Objects.requireNonNull(tipoSolicitacao, "Tipo de solicitação é obrigatório.");
        this.aprovadorPapel = aprovadorPapel;
        this.status = Objects.requireNonNull(status, "Status da aprovação é obrigatório.");
        this.criadoEmUtc = criadoEmUtc != null ? criadoEmUtc : Instant.now();
        this.decididoEmUtc = decididoEmUtc;
        this.executadoEmUtc = executadoEmUtc;
        this.solicitanteId = solicitanteId;
        this.aprovadorId = aprovadorId;
        this.decisionObservacao = decisionObservacao;
        this.actionPayloadJson = actionPayloadJson;
        this.correlationId = correlationId;
        this.mensagemErroExecucao = mensagemErroExecucao;
        this.tipoFalhaExecucao = tipoFalhaExecucao;
        this.detalheErroExecucao = detalheErroExecucao;
        this.aprovacaoPaiId = aprovacaoPaiId;
        this.conflitosJson = conflitosJson;
        this.dataOcorrenciaUtc = dataOcorrenciaUtc;
        this.justificativa = justificativa;
    }

    public Aprovacao(
            UUID id,
            UUID eventoId,
            String tipoSolicitacao,
            String aprovadorPapel,
            AprovacaoStatus status,
            Instant criadoEmUtc,
            Instant decididoEmUtc,
            Instant executadoEmUtc,
            String solicitanteId,
            String aprovadorId,
            String decisionObservacao,
            String actionPayloadJson,
            String correlationId,
            String mensagemErroExecucao,
            UUID aprovacaoPaiId,
            String conflitosJson,
            Instant dataOcorrenciaUtc,
            String justificativa) {
        this(id, eventoId, tipoSolicitacao, aprovadorPapel, status, criadoEmUtc, decididoEmUtc, executadoEmUtc,
                solicitanteId, aprovadorId, decisionObservacao, actionPayloadJson, correlationId,
                mensagemErroExecucao, null, null, aprovacaoPaiId, conflitosJson, dataOcorrenciaUtc, justificativa);
    }

    public Aprovacao(
            UUID id,
            UUID eventoId,
            String tipoSolicitacao,
            String aprovadorPapel,
            AprovacaoStatus status,
            Instant criadoEmUtc,
            Instant decididoEmUtc,
            Instant executadoEmUtc,
            String solicitanteId,
            String aprovadorId,
            String decisionObservacao,
            String actionPayloadJson,
            String correlationId,
            String mensagemErroExecucao) {
        this(id, eventoId, tipoSolicitacao, aprovadorPapel, status, criadoEmUtc, decididoEmUtc, executadoEmUtc,
                solicitanteId, aprovadorId, decisionObservacao, actionPayloadJson, correlationId,
                mensagemErroExecucao, null, null, null, null, null, null);
    }

    /** Abre uma nova solicitação em estado PENDENTE. */
    public static Aprovacao solicitar(
            UUID id,
            UUID eventoId,
            String tipoSolicitacao,
            String aprovadorPapel,
            String solicitanteId,
            String actionPayloadJson,
            Instant agora) {
        return new Aprovacao(id, eventoId, tipoSolicitacao, aprovadorPapel, AprovacaoStatus.PENDENTE,
                agora, null, null, solicitanteId, null, null, actionPayloadJson, null, null);
    }

    /** Abre uma nova solicitação com suporte a mestre-detalhe e conflitos. */
    public static Aprovacao solicitar(
            UUID id,
            UUID eventoId,
            String tipoSolicitacao,
            String aprovadorPapel,
            String solicitanteId,
            String actionPayloadJson,
            String conflitosJson,
            String justificativa,
            UUID aprovacaoPaiId,
            Instant dataOcorrenciaUtc,
            Instant agora) {
        return new Aprovacao(id, eventoId, tipoSolicitacao, aprovadorPapel, AprovacaoStatus.PENDENTE,
                agora, null, null, solicitanteId, null, null, actionPayloadJson, null, null,
                null, null, aprovacaoPaiId, conflitosJson, dataOcorrenciaUtc, justificativa);
    }

    /**
     * Garante que a solicitação ainda pode ser decidida.
     *
     * @param inicioAcao instante em que a ação de negócio começaria (pode ser {@code null})
     * @param agora      instante corrente
     */
    public void assegurarDecidivel(Instant inicioAcao, Instant agora) {
        if (this.status != AprovacaoStatus.PENDENTE) {
            throw new AprovacaoJaDecididaException("Aprovação já decidida: " + this.id);
        }
        if (inicioAcao != null && inicioAcao.isBefore(agora)) {
            throw new AprovacaoExecucaoFalhouException(
                    "Aprovacao request has expired (event already started at %s)".formatted(inicioAcao));
        }
    }

    public void aprovar(String aprovadorId, String aprovadorPapel, String observacao, Instant agora) {
        registrarDecisao(AprovacaoStatus.APROVADA, aprovadorId, aprovadorPapel, observacao, agora);
    }

    public void reprovar(String aprovadorId, String aprovadorPapel, String observacao, Instant agora) {
        registrarDecisao(AprovacaoStatus.REPROVADA, aprovadorId, aprovadorPapel, observacao, agora);
    }

    private void registrarDecisao(AprovacaoStatus novoStatus, String aprovadorId, String aprovadorPapel,
                                  String observacao, Instant agora) {
        if (this.status != AprovacaoStatus.PENDENTE) {
            throw new AprovacaoJaDecididaException("Aprovação já decidida: " + this.id);
        }
        this.status = novoStatus;
        this.aprovadorId = aprovadorId;
        this.aprovadorPapel = aprovadorPapel;
        this.decisionObservacao = observacao;
        this.decididoEmUtc = agora != null ? agora : Instant.now();
    }

    /** Marca a ação de negócio como efetivada com sucesso. Só sobre uma solicitação APROVADA. */
    public void registrarExecucaoConcluida(Instant agora) {
        assegurarAprovada();
        this.executadoEmUtc = agora != null ? agora : Instant.now();
        this.mensagemErroExecucao = null;
        this.tipoFalhaExecucao = null;
        this.detalheErroExecucao = null;
    }

    /** Registra que a ação de negócio aprovada falhou com classificação de tipo e detalhe analítico. */
    public void registrarFalhaExecucao(TipoFalhaAprovacao tipoFalha, String mensagemErro, String detalheErro) {
        assegurarAprovada();
        this.status = AprovacaoStatus.FALHA_EXECUCAO;
        this.tipoFalhaExecucao = tipoFalha != null ? tipoFalha : TipoFalhaAprovacao.SISTEMICA_APLICACAO;
        this.mensagemErroExecucao = mensagemErro;
        this.detalheErroExecucao = detalheErro;
    }

    /** Registra que a ação de negócio aprovada falhou ao ser efetivada (fallback padrão sistêmico). */
    public void registrarFalhaExecucao(String mensagemErro) {
        registrarFalhaExecucao(TipoFalhaAprovacao.SISTEMICA_APLICACAO, mensagemErro, null);
    }

    /**
     * Prepara a aprovação para reexecução. Apenas solicitações com falha sistêmica admitem reexecução.
     *
     * @param agora instante corrente da tentativa de reexecução
     */
    public void reexecutar(Instant agora) {
        if (this.status != AprovacaoStatus.FALHA_EXECUCAO) {
            throw new IllegalStateException(
                    "Apenas solicitações com falha de execução podem ser reexecutadas. Status atual: " + this.status);
        }
        if (this.tipoFalhaExecucao == TipoFalhaAprovacao.NEGOCIO_CADASTRAL) {
            throw new IllegalStateException(
                    "Solicitações com falha cadastral ou de negócio são imutáveis e não admitem reexecução "
                            + "direta; realize um novo cadastro.");
        }
        this.status = AprovacaoStatus.APROVADA;
        this.mensagemErroExecucao = null;
        this.tipoFalhaExecucao = null;
        this.detalheErroExecucao = null;
    }

    private void assegurarAprovada() {
        if (this.status != AprovacaoStatus.APROVADA) {
            throw new IllegalStateException(
                    "Só é possível registrar execução sobre uma aprovação APROVADA. Status atual: " + this.status);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public String getTipoSolicitacao() {
        return tipoSolicitacao;
    }

    public String getAprovadorPapel() {
        return aprovadorPapel;
    }

    public AprovacaoStatus getStatus() {
        return status;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public Instant getDecididoEmUtc() {
        return decididoEmUtc;
    }

    public Instant getExecutadoEmUtc() {
        return executadoEmUtc;
    }

    public String getSolicitanteId() {
        return solicitanteId;
    }

    public String getAprovadorId() {
        return aprovadorId;
    }

    public String getDecisionObservacao() {
        return decisionObservacao;
    }

    public String getActionPayloadJson() {
        return actionPayloadJson;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getMensagemErroExecucao() {
        return mensagemErroExecucao;
    }

    public TipoFalhaAprovacao getTipoFalhaExecucao() {
        return tipoFalhaExecucao;
    }

    public String getDetalheErroExecucao() {
        return detalheErroExecucao;
    }

    public UUID getAprovacaoPaiId() {
        return aprovacaoPaiId;
    }

    public String getConflitosJson() {
        return conflitosJson;
    }

    public Instant getDataOcorrenciaUtc() {
        return dataOcorrenciaUtc;
    }

    public String getJustificativa() {
        return justificativa;
    }
}
