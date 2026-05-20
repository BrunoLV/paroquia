package br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "auditoria_operacoes", schema = "calendario")
public class AuditoriaOperacaoEntity {

    @Id
    private UUID id;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "evento_id")
    private UUID contextId;

    @Column(name = "recurso_tipo", nullable = false, length = 64)
    private String recursoTipo;

    @Column(name = "recurso_id", nullable = false, length = 255)
    private String recursoId;

    @Column(nullable = false, length = 64)
    private String acao;

    @Column(nullable = false, length = 32)
    private String resultado;

    @Column(nullable = false, length = 255)
    private String ator;

    @Column(name = "ator_usuario_id")
    private UUID atorUsuarioId;

    @Column(name = "correlation_id", nullable = false, length = 128)
    private String correlationId;

    @Column(name = "detalhes_auditaveis_json", nullable = false, length = 4000)
    private String detalhesAuditaveisJson;

    @Column(name = "ocorrido_em_utc", nullable = false)
    private Instant ocorridoEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public UUID getEventoId() {
        return contextId;
    }

    public void setEventoId(UUID contextId) {
        this.contextId = contextId;
    }

    public String getRecursoTipo() {
        return recursoTipo;
    }

    public void setRecursoTipo(String recursoTipo) {
        this.recursoTipo = recursoTipo;
    }

    public String getRecursoId() {
        return recursoId;
    }

    public void setRecursoId(String recursoId) {
        this.recursoId = recursoId;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getAtor() {
        return ator;
    }

    public void setAtor(String ator) {
        this.ator = ator;
    }

    public UUID getAtorUsuarioId() {
        return atorUsuarioId;
    }

    public void setAtorUsuarioId(UUID atorUsuarioId) {
        this.atorUsuarioId = atorUsuarioId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getDetalhesAuditaveisJson() {
        return detalhesAuditaveisJson;
    }

    public void setDetalhesAuditaveisJson(String detalhesAuditaveisJson) {
        this.detalhesAuditaveisJson = detalhesAuditaveisJson;
    }

    public Instant getOcorridoEmUtc() {
        return ocorridoEmUtc;
    }

    public void setOcorridoEmUtc(Instant ocorridoEmUtc) {
        this.ocorridoEmUtc = ocorridoEmUtc;
    }
}
