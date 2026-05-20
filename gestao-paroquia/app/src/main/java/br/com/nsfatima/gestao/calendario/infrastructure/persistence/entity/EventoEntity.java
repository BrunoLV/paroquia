package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "eventos", schema = "calendario")
public class EventoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(length = 4000)
    private String descricao;

    @Column(name = "organizacao_responsavel_id", nullable = false)
    private UUID organizacaoResponsavelId;

    @Column(name = "inicio_utc", nullable = false)
    private Instant inicioUtc;

    @Column(name = "fim_utc", nullable = false)
    private Instant fimUtc;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "cancelado_motivo", length = 2000)
    private String canceladoMotivo;

    @Column(name = "adicionado_extra_justificativa", length = 4000)
    private String adicionadoExtraJustificativa;

    @Column(name = "conflict_state", length = 32)
    private String conflictState;

    @Column(name = "conflict_reason", length = 2000)
    private String conflictReason;

    @Column(name = "recorrencia_id")
    private UUID recorrenciaId;

    @Column(name = "projeto_id")
    private UUID projetoId;

    @Column(name = "categoria", length = 32)
    private String categoria;

    @Column(name = "local_id")
    private UUID localId;

    @OneToMany
    @JoinColumn(name = "evento_id", insertable = false, updatable = false)
    private List<EventoEnvolvidoEntity> envolvidos;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public UUID getOrganizacaoResponsavelId() {
        return organizacaoResponsavelId;
    }

    public void setOrganizacaoResponsavelId(UUID organizacaoResponsavelId) {
        this.organizacaoResponsavelId = organizacaoResponsavelId;
    }

    public Instant getInicioUtc() {
        return inicioUtc;
    }

    public void setInicioUtc(Instant inicioUtc) {
        this.inicioUtc = inicioUtc;
    }

    public Instant getFimUtc() {
        return fimUtc;
    }

    public void setFimUtc(Instant fimUtc) {
        this.fimUtc = fimUtc;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCanceladoMotivo() {
        return canceladoMotivo;
    }

    public void setCanceladoMotivo(String canceladoMotivo) {
        this.canceladoMotivo = canceladoMotivo;
    }

    public String getAdicionadoExtraJustificativa() {
        return adicionadoExtraJustificativa;
    }

    public void setAdicionadoExtraJustificativa(String adicionadoExtraJustificativa) {
        this.adicionadoExtraJustificativa = adicionadoExtraJustificativa;
    }

    public String getConflictState() {
        return conflictState;
    }

    public void setConflictState(String conflictState) {
        this.conflictState = conflictState;
    }

    public String getConflictReason() {
        return conflictReason;
    }

    public void setConflictReason(String conflictReason) {
        this.conflictReason = conflictReason;
    }

    public UUID getRecorrenciaId() {
        return recorrenciaId;
    }

    public void setRecorrenciaId(UUID recorrenciaId) {
        this.recorrenciaId = recorrenciaId;
    }

    public UUID getProjetoId() {
        return projetoId;
    }

    public void setProjetoId(UUID projetoId) {
        this.projetoId = projetoId;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public UUID getLocalId() {
        return localId;
    }

    public void setLocalId(UUID localId) {
        this.localId = localId;
    }

    public List<EventoEnvolvidoEntity> getEnvolvidos() {
        return envolvidos;
    }

    public void setEnvolvidos(List<EventoEnvolvidoEntity> envolvidos) {
        this.envolvidos = envolvidos;
    }
}
