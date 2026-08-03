package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Representa uma ocupação de um ativo patrimonial num período: alocação a um evento (podendo ser
 * empréstimo entre organizações, quando {@code organizacaoOrigemId} e {@code organizacaoDestinoId}
 * divergem) ou período de manutenção. Empréstimos nascem {@code PENDENTE_APROVACAO} e só viram
 * {@code CONFIRMADA} após decisão do fluxo de aprovação vinculado por {@code aprovacaoId}.
 */
@Entity
@Table(name = "alocacoes_ativos", schema = "calendario")
public class AtivoAlocacaoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "ativo_id", nullable = false)
    private UUID ativoId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AtivoAlocacaoTipo tipo;

    @Column(name = "evento_id")
    private UUID eventoId;

    @Column(name = "organizacao_origem_id", nullable = false)
    private UUID organizacaoOrigemId;

    @Column(name = "organizacao_destino_id")
    private UUID organizacaoDestinoId;

    @Column(nullable = false)
    private boolean emprestimo;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private AtivoAlocacaoStatus status;

    @Column(name = "solicitante_id")
    private UUID solicitanteId;

    @Column(length = 4000)
    private String justificativa;

    @Column(name = "aprovacao_id")
    private UUID aprovacaoId;

    @Column(name = "impactada_por_atraso", nullable = false)
    private boolean impactadaPorAtraso;

    @Column(name = "alerta_atraso_emitido_em")
    private Instant alertaAtrasoEmitidoEm;

    @Column(name = "motivo_cancelamento", length = 4000)
    private String motivoCancelamento;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "devolvido_em_utc")
    private Instant devolvidoEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAtivoId() {
        return ativoId;
    }

    public void setAtivoId(UUID ativoId) {
        this.ativoId = ativoId;
    }

    public AtivoAlocacaoTipo getTipo() {
        return tipo;
    }

    public void setTipo(AtivoAlocacaoTipo tipo) {
        this.tipo = tipo;
    }

    public UUID getEventoId() {
        return eventoId;
    }

    public void setEventoId(UUID eventoId) {
        this.eventoId = eventoId;
    }

    public UUID getOrganizacaoOrigemId() {
        return organizacaoOrigemId;
    }

    public void setOrganizacaoOrigemId(UUID organizacaoOrigemId) {
        this.organizacaoOrigemId = organizacaoOrigemId;
    }

    public UUID getOrganizacaoDestinoId() {
        return organizacaoDestinoId;
    }

    public void setOrganizacaoDestinoId(UUID organizacaoDestinoId) {
        this.organizacaoDestinoId = organizacaoDestinoId;
    }

    public boolean isEmprestimo() {
        return emprestimo;
    }

    public void setEmprestimo(boolean emprestimo) {
        this.emprestimo = emprestimo;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public LocalDate getDataFim() {
        return dataFim;
    }

    public void setDataFim(LocalDate dataFim) {
        this.dataFim = dataFim;
    }

    public AtivoAlocacaoStatus getStatus() {
        return status;
    }

    public void setStatus(AtivoAlocacaoStatus status) {
        this.status = status;
    }

    public UUID getSolicitanteId() {
        return solicitanteId;
    }

    public void setSolicitanteId(UUID solicitanteId) {
        this.solicitanteId = solicitanteId;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public void setJustificativa(String justificativa) {
        this.justificativa = justificativa;
    }

    public UUID getAprovacaoId() {
        return aprovacaoId;
    }

    public void setAprovacaoId(UUID aprovacaoId) {
        this.aprovacaoId = aprovacaoId;
    }

    public boolean isImpactadaPorAtraso() {
        return impactadaPorAtraso;
    }

    public void setImpactadaPorAtraso(boolean impactadaPorAtraso) {
        this.impactadaPorAtraso = impactadaPorAtraso;
    }

    public Instant getAlertaAtrasoEmitidoEm() {
        return alertaAtrasoEmitidoEm;
    }

    public void setAlertaAtrasoEmitidoEm(Instant alertaAtrasoEmitidoEm) {
        this.alertaAtrasoEmitidoEm = alertaAtrasoEmitidoEm;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant criadoEmUtc) {
        this.criadoEmUtc = criadoEmUtc;
    }

    public Instant getDevolvidoEmUtc() {
        return devolvidoEmUtc;
    }

    public void setDevolvidoEmUtc(Instant devolvidoEmUtc) {
        this.devolvidoEmUtc = devolvidoEmUtc;
    }
}
