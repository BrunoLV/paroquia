package br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projeto_solicitacoes_aprovacao", schema = "calendario")
public class ProjetoSolicitacaoAprovacaoEntity {

    @Id
    private UUID id;

    @Column(name = "projeto_id")
    private UUID projetoId;

    @Column(name = "tipo_solicitacao", nullable = false, length = 64)
    private String tipoSolicitacao;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "solicitante_id", nullable = false)
    private String solicitanteId;

    @Column(name = "solicitante_papel", nullable = false, length = 64)
    private String solicitantePapel;

    @Column(name = "solicitante_tipo_organizacao", nullable = false, length = 64)
    private String solicitanteTipoOrganizacao;

    @Column(name = "action_payload_json", length = 4000)
    private String actionPayloadJson;

    @Column(name = "cancelar_eventos_vinculados")
    private Boolean cancelarEventosVinculados = false;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "decidido_em_utc")
    private Instant decididoEmUtc;

    @Column(name = "aprovador_id")
    private String aprovadorId;

    @Column(name = "decision_observacao", length = 2000)
    private String decisionObservacao;

    private Long version = 0L;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getProjetoId() {
        return projetoId;
    }

    public void setProjetoId(UUID projetoId) {
        this.projetoId = projetoId;
    }

    public String getTipoSolicitacao() {
        return tipoSolicitacao;
    }

    public void setTipoSolicitacao(String tipoSolicitacao) {
        this.tipoSolicitacao = tipoSolicitacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getActionPayloadJson() {
        return actionPayloadJson;
    }

    public void setActionPayloadJson(String actionPayloadJson) {
        this.actionPayloadJson = actionPayloadJson;
    }

    public Boolean getCancelarEventosVinculados() {
        return cancelarEventosVinculados;
    }

    public void setCancelarEventosVinculados(Boolean cancelarEventosVinculados) {
        this.cancelarEventosVinculados = cancelarEventosVinculados;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant criadoEmUtc) {
        this.criadoEmUtc = criadoEmUtc;
    }

    public Instant getDecididoEmUtc() {
        return decididoEmUtc;
    }

    public void setDecididoEmUtc(Instant decididoEmUtc) {
        this.decididoEmUtc = decididoEmUtc;
    }

    public String getAprovadorId() {
        return aprovadorId;
    }

    public void setAprovadorId(String aprovadorId) {
        this.aprovadorId = aprovadorId;
    }

    public String getDecisionObservacao() {
        return decisionObservacao;
    }

    public void setDecisionObservacao(String decisionObservacao) {
        this.decisionObservacao = decisionObservacao;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
