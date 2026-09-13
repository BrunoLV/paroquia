package br.com.nsfatima.gestao.reserva.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade relacional JPA para mapeamento da tabela calendario.reservas.
 */
@Entity
@Table(name = "reservas", schema = "calendario")
public class ReservaEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "tipo_item", nullable = false, length = 20)
    private String tipoItem;

    @Column(name = "item_id", nullable = false)
    private UUID itemId;

    @Column(name = "data_hora_inicio", nullable = false)
    private Instant dataHoraInicio;

    @Column(name = "data_hora_fim", nullable = false)
    private Instant dataHoraFim;

    @Column(name = "buffer_minutos", nullable = false)
    private int bufferMinutos;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "solicitante_id", nullable = false)
    private UUID solicitanteId;

    @Column(name = "pastoral_movimento_id", nullable = false)
    private UUID pastoralMovimentoId;

    @Column(name = "evento_id")
    private UUID eventoId;

    @Column(name = "grupo_recorrencia_id")
    private UUID grupoRecorrenciaId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "aprovador_id")
    private UUID aprovadorId;

    @Column(name = "aprovado_em")
    private Instant aprovadoEm;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @Column(name = "cancelado_por_id")
    private UUID canceladoPorId;

    @Column(name = "check_avarias", columnDefinition = "TEXT")
    private String checkAvarias;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public ReservaEntity() {
    }

    public ReservaEntity(
            UUID id,
            String tipoItem,
            UUID itemId,
            Instant dataHoraInicio,
            Instant dataHoraFim,
            int bufferMinutos,
            String descricao,
            UUID solicitanteId,
            UUID pastoralMovimentoId,
            UUID eventoId,
            UUID grupoRecorrenciaId,
            String status,
            UUID aprovadorId,
            Instant aprovadoEm,
            String motivoCancelamento,
            UUID canceladoPorId,
            String checkAvarias,
            Instant createdAt,
            Instant updatedAt,
            Instant deletedAt,
            long version
    ) {
        this.id = id;
        this.tipoItem = tipoItem;
        this.itemId = itemId;
        this.dataHoraInicio = dataHoraInicio;
        this.dataHoraFim = dataHoraFim;
        this.bufferMinutos = bufferMinutos;
        this.descricao = descricao;
        this.solicitanteId = solicitanteId;
        this.pastoralMovimentoId = pastoralMovimentoId;
        this.eventoId = eventoId;
        this.grupoRecorrenciaId = grupoRecorrenciaId;
        this.status = status;
        this.aprovadorId = aprovadorId;
        this.aprovadoEm = aprovadoEm;
        this.motivoCancelamento = motivoCancelamento;
        this.canceladoPorId = canceladoPorId;
        this.checkAvarias = checkAvarias;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.deletedAt = deletedAt;
        this.version = version;
    }

    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTipoItem() {
        return tipoItem;
    }

    public void setTipoItem(String tipoItem) {
        this.tipoItem = tipoItem;
    }

    public UUID getItemId() {
        return itemId;
    }

    public void setItemId(UUID itemId) {
        this.itemId = itemId;
    }

    public Instant getDataHoraInicio() {
        return dataHoraInicio;
    }

    public void setDataHoraInicio(Instant dataHoraInicio) {
        this.dataHoraInicio = dataHoraInicio;
    }

    public Instant getDataHoraFim() {
        return dataHoraFim;
    }

    public void setDataHoraFim(Instant dataHoraFim) {
        this.dataHoraFim = dataHoraFim;
    }

    public int getBufferMinutos() {
        return bufferMinutos;
    }

    public void setBufferMinutos(int bufferMinutos) {
        this.bufferMinutos = bufferMinutos;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public void setSolicitanteId(UUID solicitanteId) {
        this.solicitanteId = solicitanteId;
    }

    public UUID getPastoralMovimentoId() {
        return pastoralMovimentoId;
    }

    public void setPastoralMovimentoId(UUID pastoralMovimentoId) {
        this.pastoralMovimentoId = pastoralMovimentoId;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public void setEventoId(UUID eventoId) {
        this.eventoId = eventoId;
    }

    public UUID getGrupoRecorrenciaId() {
        return grupoRecorrenciaId;
    }

    public void setGrupoRecorrenciaId(UUID grupoRecorrenciaId) {
        this.grupoRecorrenciaId = grupoRecorrenciaId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getAprovadorId() {
        return aprovadorId;
    }

    public void setAprovadorId(UUID aprovadorId) {
        this.aprovadorId = aprovadorId;
    }

    public Instant getAprovadoEm() {
        return aprovadoEm;
    }

    public void setAprovadoEm(Instant aprovadoEm) {
        this.aprovadoEm = aprovadoEm;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public UUID getCanceladoPorId() {
        return canceladoPorId;
    }

    public void setCanceladoPorId(UUID canceladoPorId) {
        this.canceladoPorId = canceladoPorId;
    }

    public String getCheckAvarias() {
        return checkAvarias;
    }

    public void setCheckAvarias(String checkAvarias) {
        this.checkAvarias = checkAvarias;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
