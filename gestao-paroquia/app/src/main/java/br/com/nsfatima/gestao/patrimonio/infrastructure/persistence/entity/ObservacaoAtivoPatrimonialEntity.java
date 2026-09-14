package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Nota de observação no histórico de um ativo patrimonial — parte da trilha obrigatória de
 * justificativa de uso (alocação/devolução), não uma anotação livre. Suporta remoção lógica
 * (campos {@code removida}/{@code removidaEmUtc}/{@code removidaPorUsuarioId}) preservando o
 * registro para auditoria.
 */
@Entity
@Table(name = "observacoes_ativo_patrimonial", schema = "calendario")
public class ObservacaoAtivoPatrimonialEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(name = "ativo_patrimonial_id", nullable = false)
    private UUID ativoPatrimonialId;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    /** Nome de {@link br.com.nsfatima.gestao.patrimonio.domain.model.TipoNotaAtivoPatrimonial}. */
    @Column(nullable = false, length = 64)
    private String tipo;

    @Column(nullable = false, length = 4000)
    private String conteudo;

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

    public UUID getAtivoPatrimonialId() {
        return ativoPatrimonialId;
    }

    public void setAtivoPatrimonialId(UUID ativoPatrimonialId) {
        this.ativoPatrimonialId = ativoPatrimonialId;
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
