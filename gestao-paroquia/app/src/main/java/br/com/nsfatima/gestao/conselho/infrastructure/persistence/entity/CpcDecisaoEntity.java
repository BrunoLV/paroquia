package br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
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
 * Entidade JPA de uma ata/decisão de conselho paroquial (CPC/CPP). Persiste tanto a proposta
 * inicial (solicitante, pauta, reunião) quanto o resultado da homologação (homologador, motivo
 * de veto, data da decisão) na mesma linha — {@code homologadorId}, {@code motivoVeto} e
 * {@code decididoEmUtc} permanecem nulos enquanto {@code statusHomologacao} for
 * {@code PENDENTE_HOMOLOGACAO}.
 */
@Entity
@Table(name = "cpc_decisoes", schema = "calendario")
public class CpcDecisaoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "pauta_descricao", nullable = false, length = 4000)
    private String pautaDescricao;

    @Column(name = "data_reuniao", nullable = false)
    private LocalDate dataReuniao;

    @Column(name = "comunidade_id", nullable = false)
    private UUID comunidadeId;

    @Column(name = "conselho_tipo", nullable = false, length = 32)
    private String conselhoTipo = "CPC";

    @Enumerated(EnumType.STRING)
    @Column(name = "status_homologacao", nullable = false, length = 32)
    private StatusHomologacao statusHomologacao = StatusHomologacao.PENDENTE_HOMOLOGACAO;

    @Column(name = "solicitante_id", nullable = false, length = 128)
    private String solicitanteId;

    @Column(name = "solicitante_nome", length = 160)
    private String solicitanteNome;

    @Column(name = "homologador_id", length = 128)
    private String homologadorId;

    @Column(name = "motivo_veto", length = 2000)
    private String motivoVeto;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "decidido_em_utc")
    private Instant decididoEmUtc;

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

    public String getPautaDescricao() {
        return pautaDescricao;
    }

    public void setPautaDescricao(String pautaDescricao) {
        this.pautaDescricao = pautaDescricao;
    }

    public LocalDate getDataReuniao() {
        return dataReuniao;
    }

    public void setDataReuniao(LocalDate dataReuniao) {
        this.dataReuniao = dataReuniao;
    }

    public UUID getComunidadeId() {
        return comunidadeId;
    }

    public void setComunidadeId(UUID comunidadeId) {
        this.comunidadeId = comunidadeId;
    }

    public String getConselhoTipo() {
        return conselhoTipo;
    }

    public void setConselhoTipo(String conselhoTipo) {
        this.conselhoTipo = conselhoTipo;
    }

    public StatusHomologacao getStatusHomologacao() {
        return statusHomologacao;
    }

    public void setStatusHomologacao(StatusHomologacao statusHomologacao) {
        this.statusHomologacao = statusHomologacao;
    }

    public String getSolicitanteId() {
        return solicitanteId;
    }

    public void setSolicitanteId(String solicitanteId) {
        this.solicitanteId = solicitanteId;
    }

    public String getSolicitanteNome() {
        return solicitanteNome;
    }

    public void setSolicitanteNome(String solicitanteNome) {
        this.solicitanteNome = solicitanteNome;
    }

    public String getHomologadorId() {
        return homologadorId;
    }

    public void setHomologadorId(String homologadorId) {
        this.homologadorId = homologadorId;
    }

    public String getMotivoVeto() {
        return motivoVeto;
    }

    public void setMotivoVeto(String motivoVeto) {
        this.motivoVeto = motivoVeto;
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
}
