package br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "aprovacoes", schema = "calendario")
public class AprovacaoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "evento_id")
    private UUID eventoId;

    @Column(name = "tipo_solicitacao", nullable = false, length = 64)
    private String tipoSolicitacao;

    @Column(name = "aprovador_papel", nullable = false, length = 64)
    private String aprovadorPapel;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "decidido_em_utc")
    private Instant decididoEmUtc;

    @Column(name = "executado_em_utc")
    private Instant executadoEmUtc;

    @Column(name = "solicitante_id", length = 255)
    private String solicitanteId;

    @Column(name = "solicitante_papel", length = 64)
    private String solicitantePapel;

    @Column(name = "solicitante_tipo_organizacao", length = 64)
    private String solicitanteTipoOrganizacao;

    @Column(name = "aprovador_id", length = 255)
    private String aprovadorId;

    @Column(name = "motivo_cancelamento_snapshot", length = 2000)
    private String motivoCancelamentoSnapshot;

    @Column(name = "decision_observacao", length = 2000)
    private String decisionObservacao;

    @Column(name = "action_payload_json", length = 4000)
    private String actionPayloadJson;

    @Column(name = "correlation_id", length = 128)
    private String correlationId;

    @Column(name = "mensagem_erro_execucao", length = 2000)
    private String mensagemErroExecucao;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public void setEventoId(UUID eventoId) {
        this.eventoId = eventoId;
    }

    public String getTipoSolicitacao() {
        return tipoSolicitacao;
    }

    public void setTipoSolicitacao(String tipoSolicitacao) {
        this.tipoSolicitacao = tipoSolicitacao;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant criadoEmUtc) {
        this.criadoEmUtc = criadoEmUtc;
    }

    public String getAprovadorPapel() {
        return aprovadorPapel;
    }

    public AprovadorPapel getAprovadorPapelEnum() {
        return AprovadorPapel.fromStoredValue(aprovadorPapel);
    }

    public void setAprovadorPapel(String aprovadorPapel) {
        this.aprovadorPapel = aprovadorPapel;
    }

    public void setAprovadorPapel(AprovadorPapel aprovadorPapel) {
        this.aprovadorPapel = aprovadorPapel == null ? null : aprovadorPapel.storedValue();
    }

    public String getStatus() {
        return status;
    }

    public AprovacaoStatus getStatusEnum() {
        return AprovacaoStatus.fromJson(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus(AprovacaoStatus status) {
        this.status = status == null ? null : status.name();
    }

    public Instant getDecididoEmUtc() {
        return decididoEmUtc;
    }

    public void setDecididoEmUtc(Instant decididoEmUtc) {
        this.decididoEmUtc = decididoEmUtc;
    }

    public Instant getExecutadoEmUtc() {
        return executadoEmUtc;
    }

    public void setExecutadoEmUtc(Instant executadoEmUtc) {
        this.executadoEmUtc = executadoEmUtc;
    }

    public String getSolicitanteId() {
        return solicitanteId;
    }

    public void setSolicitanteId(String solicitanteId) {
        this.solicitanteId = solicitanteId;
    }

    public String getSolicitantePapel() {
        return solicitantePapel;
    }

    public void setSolicitantePapel(String solicitantePapel) {
        this.solicitantePapel = solicitantePapel;
    }

    public String getSolicitanteTipoOrganizacao() {
        return solicitanteTipoOrganizacao;
    }

    public void setSolicitanteTipoOrganizacao(String solicitanteTipoOrganizacao) {
        this.solicitanteTipoOrganizacao = solicitanteTipoOrganizacao;
    }

    public String getAprovadorId() {
        return aprovadorId;
    }

    public void setAprovadorId(String aprovadorId) {
        this.aprovadorId = aprovadorId;
    }

    public String getMotivoCancelamentoSnapshot() {
        return motivoCancelamentoSnapshot;
    }

    public void setMotivoCancelamentoSnapshot(String motivoCancelamentoSnapshot) {
        this.motivoCancelamentoSnapshot = motivoCancelamentoSnapshot;
    }

    public String getDecisionObservacao() {
        return decisionObservacao;
    }

    public void setDecisionObservacao(String decisionObservacao) {
        this.decisionObservacao = decisionObservacao;
    }

    public String getActionPayloadJson() {
        return actionPayloadJson;
    }

    public void setActionPayloadJson(String actionPayloadJson) {
        this.actionPayloadJson = actionPayloadJson;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getMensagemErroExecucao() {
        return mensagemErroExecucao;
    }

    public void setMensagemErroExecucao(String mensagemErroExecucao) {
        this.mensagemErroExecucao = mensagemErroExecucao;
    }
}
