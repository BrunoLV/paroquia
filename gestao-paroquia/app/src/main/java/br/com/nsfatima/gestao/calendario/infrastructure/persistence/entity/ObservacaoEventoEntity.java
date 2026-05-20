package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "observacoes_evento", schema = "calendario")
public class ObservacaoEventoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "evento_id", nullable = false)
    private UUID eventoId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @Column(nullable = false, length = 64)
    private String tipo;

    @Column(nullable = false, length = 4000)
    private String conteudo;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "removida", nullable = false)
    private boolean removida;

    @Column(name = "removida_em_utc")
    private Instant removidaEmUtc;

    @Column(name = "removida_por_usuario_id")
    private UUID removidaPorUsuarioId;

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

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant criadoEmUtc) {
        this.criadoEmUtc = criadoEmUtc;
    }

    public boolean isRemovida() {
        return removida;
    }

    public void setRemovida(boolean removida) {
        this.removida = removida;
    }

    public Instant getRemovidaEmUtc() {
        return removidaEmUtc;
    }

    public void setRemovidaEmUtc(Instant removidaEmUtc) {
        this.removidaEmUtc = removidaEmUtc;
    }

    public UUID getRemovidaPorUsuarioId() {
        return removidaPorUsuarioId;
    }

    public void setRemovidaPorUsuarioId(UUID removidaPorUsuarioId) {
        this.removidaPorUsuarioId = removidaPorUsuarioId;
    }
}
