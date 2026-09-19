package br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.converter.TipoGrupoAttributeConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade JPA que representa uma solicitação de aprovação pendente sobre um {@code Projeto},
 * criada quando um ator sem nível de autorização suficiente tenta uma ação sensível (hoje,
 * apenas cancelamento, tipo {@code CANCELAMENTO}; ver
 * {@code TransicionarProjetoUseCase#cancelar}) ou quando a criação do projeto em si exige
 * aprovação (tipo {@code CRIACAO}). O {@code actionPayloadJson} guarda os dados necessários
 * para efetivar a ação quando a solicitação for aprovada (ex.: o {@code CriarProjetoRequest}
 * serializado, no caso de {@code CRIACAO}). Enquanto {@code status} permanecer
 * {@code PENDENTE}, nenhuma mudança foi aplicada ao projeto correspondente.
 */
@Entity
@Table(name = "projeto_solicitacoes_aprovacao", schema = "calendario")
public class ProjetoSolicitacaoAprovacaoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(name = "projeto_id")
    private UUID projetoId;

    @Column(name = "tipo_solicitacao", nullable = false, length = 64)
    private String tipoSolicitacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AprovacaoStatus status;

    @Column(name = "solicitante_id", nullable = false)
    private String solicitanteId;

    @Column(name = "solicitante_papel", nullable = false, length = 64)
    private String solicitantePapel;

    @Convert(converter = TipoGrupoAttributeConverter.class)
    @Column(name = "solicitante_tipo_grupo_paroquial", nullable = false, length = 64)
    private TipoGrupo solicitanteTipoGrupo;

    @Column(name = "action_payload_json", length = 4000)
    private String actionPayloadJson;

    @Column(name = "cancelar_eventos_vinculados")
    private Boolean cancelarEventosVinculados = false;

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

    public AprovacaoStatus getStatus() {
        return status;
    }

    public void setStatus(AprovacaoStatus status) {
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

    public TipoGrupo getSolicitanteTipoGrupo() {
        return solicitanteTipoGrupo;
    }

    public void setSolicitanteTipoGrupo(TipoGrupo solicitanteTipoGrupo) {
        this.solicitanteTipoGrupo = solicitanteTipoGrupo;
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
