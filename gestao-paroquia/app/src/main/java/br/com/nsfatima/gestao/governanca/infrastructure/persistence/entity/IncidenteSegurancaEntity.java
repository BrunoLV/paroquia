package br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.governanca.domain.model.GravidadeIncidente;
import br.com.nsfatima.gestao.governanca.domain.model.StatusIncidente;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade JPA para persistência dos registros formais de incidentes de segurança e violações de dados (Art. 48 LGPD).
 */
@Entity
@Table(name = "incidentes_seguranca", schema = "calendario")
public class IncidenteSegurancaEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "tipo_dado_afetado", nullable = false, length = 100)
    private String tipoDadoAfetado;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria_gravidade", nullable = false, length = 30)
    private GravidadeIncidente categoriaGravidade;

    @Column(name = "data_ocorrencia", nullable = false)
    private Instant dataOcorrencia;

    @Column(name = "data_deteccao", nullable = false)
    private Instant dataDeteccao;

    @Column(name = "medidas_mitigacao", columnDefinition = "TEXT")
    private String medidasMitigacao;

    @Column(name = "notificado_anpd", nullable = false)
    private boolean notificadoAnpd;

    @Column(name = "data_notificacao_anpd")
    private Instant dataNotificacaoAnpd;

    @Column(name = "protocolo_anpd", length = 100)
    private String protocoloAnpd;

    @Column(name = "notificado_titulares", nullable = false)
    private boolean notificadoTitulares;

    @Column(name = "data_notificacao_titulares")
    private Instant dataNotificacaoTitulares;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusIncidente status = StatusIncidente.EM_ANALISE;

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

    public String getTipoDadoAfetado() {
        return tipoDadoAfetado;
    }

    public void setTipoDadoAfetado(String tipoDadoAfetado) {
        this.tipoDadoAfetado = tipoDadoAfetado;
    }

    public GravidadeIncidente getCategoriaGravidade() {
        return categoriaGravidade;
    }

    public void setCategoriaGravidade(GravidadeIncidente categoriaGravidade) {
        this.categoriaGravidade = categoriaGravidade;
    }

    public Instant getDataOcorrencia() {
        return dataOcorrencia;
    }

    public void setDataOcorrencia(Instant dataOcorrencia) {
        this.dataOcorrencia = dataOcorrencia;
    }

    public Instant getDataDeteccao() {
        return dataDeteccao;
    }

    public void setDataDeteccao(Instant dataDeteccao) {
        this.dataDeteccao = dataDeteccao;
    }

    public String getMedidasMitigacao() {
        return medidasMitigacao;
    }

    public void setMedidasMitigacao(String medidasMitigacao) {
        this.medidasMitigacao = medidasMitigacao;
    }

    public boolean isNotificadoAnpd() {
        return notificadoAnpd;
    }

    public void setNotificadoAnpd(boolean notificadoAnpd) {
        this.notificadoAnpd = notificadoAnpd;
    }

    public Instant getDataNotificacaoAnpd() {
        return dataNotificacaoAnpd;
    }

    public void setDataNotificacaoAnpd(Instant dataNotificacaoAnpd) {
        this.dataNotificacaoAnpd = dataNotificacaoAnpd;
    }

    public String getProtocoloAnpd() {
        return protocoloAnpd;
    }

    public void setProtocoloAnpd(String protocoloAnpd) {
        this.protocoloAnpd = protocoloAnpd;
    }

    public boolean isNotificadoTitulares() {
        return notificadoTitulares;
    }

    public void setNotificadoTitulares(boolean notificadoTitulares) {
        this.notificadoTitulares = notificadoTitulares;
    }

    public Instant getDataNotificacaoTitulares() {
        return dataNotificacaoTitulares;
    }

    public void setDataNotificacaoTitulares(Instant dataNotificacaoTitulares) {
        this.dataNotificacaoTitulares = dataNotificacaoTitulares;
    }

    public StatusIncidente getStatus() {
        return status;
    }

    public void setStatus(StatusIncidente status) {
        this.status = status;
    }
}
