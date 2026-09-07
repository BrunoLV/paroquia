package br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.clero.domain.model.TipoPessoaFamiliar;
import br.com.nsfatima.gestao.fiel.domain.model.TipoRelacionamentoFamiliar;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

/** Aresta direcionada de parentesco que pode ligar fiéis e clérigos. */
@Entity
@Table(name = "relacionamentos_familiares", schema = "calendario")
public class RelacionamentoFamiliarEntity {
    @Id
    private UUID id;

    @Column(name = "par_id", nullable = false)
    private UUID parId;

    @Column(name = "pessoa_origem_id", nullable = false)
    private UUID pessoaOrigemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa_origem", nullable = false)
    private TipoPessoaFamiliar tipoPessoaOrigem;

    @Column(name = "pessoa_destino_id", nullable = false)
    private UUID pessoaDestinoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pessoa_destino", nullable = false)
    private TipoPessoaFamiliar tipoPessoaDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_relacao", nullable = false)
    private TipoRelacionamentoFamiliar tipo;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID valor) {
        id = valor;
    }

    public UUID getParId() {
        return parId;
    }

    public void setParId(UUID valor) {
        parId = valor;
    }

    public UUID getPessoaOrigemId() {
        return pessoaOrigemId;
    }

    public void setPessoaOrigemId(UUID valor) {
        pessoaOrigemId = valor;
    }

    public TipoPessoaFamiliar getTipoPessoaOrigem() {
        return tipoPessoaOrigem;
    }

    public void setTipoPessoaOrigem(TipoPessoaFamiliar valor) {
        tipoPessoaOrigem = valor;
    }

    public UUID getPessoaDestinoId() {
        return pessoaDestinoId;
    }

    public void setPessoaDestinoId(UUID valor) {
        pessoaDestinoId = valor;
    }

    public TipoPessoaFamiliar getTipoPessoaDestino() {
        return tipoPessoaDestino;
    }

    public void setTipoPessoaDestino(TipoPessoaFamiliar valor) {
        tipoPessoaDestino = valor;
    }

    public TipoRelacionamentoFamiliar getTipo() {
        return tipo;
    }

    public void setTipo(TipoRelacionamentoFamiliar valor) {
        tipo = valor;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant valor) {
        criadoEmUtc = valor;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant valor) {
        deletedAt = valor;
    }
}
