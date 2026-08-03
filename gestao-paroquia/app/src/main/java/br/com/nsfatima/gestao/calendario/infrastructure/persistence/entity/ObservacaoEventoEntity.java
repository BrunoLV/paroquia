package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Registra uma observação/anotação textual feita por um usuário sobre um evento (ex.: nota
 * de acompanhamento, comentário de revisão). Suporta remoção lógica (soft delete) em vez de
 * exclusão física, preservando o histórico de quem e quando removeu a observação; edições
 * de conteúdo são auditadas separadamente em {@link ObservacaoNotaRevisaoEntity}.
 */
@Entity
@Table(name = "observacoes_evento", schema = "calendario")
public class ObservacaoEventoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "evento_id", nullable = false)
    private UUID eventoId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    /** Classificação livre da observação (ex.: categoria/tipo de nota), definida pelo domínio de uso. */
    @Column(nullable = false, length = 64)
    private String tipo;

    @Column(nullable = false, length = 4000)
    private String conteudo;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    /** Indica remoção lógica (soft delete): {@code true} quando a observação foi removida e não deve mais ser exibida ativamente. */
    @Column(name = "removida", nullable = false)
    private boolean removida;

    /** Instante (UTC) da remoção lógica; preenchido somente quando {@link #removida} é {@code true}. */
    @Column(name = "removida_em_utc")
    private Instant removidaEmUtc;

    /** Usuário que efetuou a remoção lógica; preenchido somente quando {@link #removida} é {@code true}. */
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
