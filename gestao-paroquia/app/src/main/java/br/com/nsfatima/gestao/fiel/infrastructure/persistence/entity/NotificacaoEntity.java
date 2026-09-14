package br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Aviso persistente sem PII para comunicação de decisões do módulo. */
@Entity
@Table(name = "notificacoes", schema = "calendario")
public class NotificacaoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    private String tipo;
    private String mensagem;

    @Column(name = "lida_em_utc")
    private Instant lidaEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID v) {
        id = v;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID v) {
        usuarioId = v;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String v) {
        tipo = v;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String v) {
        mensagem = v;
    }

    public Instant getLidaEmUtc() {
        return lidaEmUtc;
    }

    public void setLidaEmUtc(Instant v) {
        lidaEmUtc = v;
    }
}
