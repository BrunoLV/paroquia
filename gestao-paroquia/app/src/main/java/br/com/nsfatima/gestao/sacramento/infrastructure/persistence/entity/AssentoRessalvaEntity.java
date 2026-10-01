package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Entidade JPA de {@code calendario.assento_ressalvas}: retificação atestada, append-only. */
@Entity
@Table(name = "assento_ressalvas", schema = "calendario")
public class AssentoRessalvaEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assento_id", nullable = false)
    private AssentoSacramentalEntity assento;

    @Column(name = "campo", nullable = false, length = 128)
    private String campo;

    @Column(name = "valor_original", columnDefinition = "TEXT")
    private String valorOriginal;

    @Column(name = "valor_corrigido", nullable = false, columnDefinition = "TEXT")
    private String valorCorrigido;

    @Column(name = "justificativa", nullable = false, columnDefinition = "TEXT")
    private String justificativa;

    @Column(name = "atestada_por", length = 128)
    private String atestadaPor;

    @Column(name = "atestada_em_utc", nullable = false)
    private Instant atestadaEmUtc;

    public AssentoRessalvaEntity() {
    }

    public AssentoRessalvaEntity(UUID id, AssentoSacramentalEntity assento, String campo, String valorOriginal,
                                 String valorCorrigido, String justificativa, String atestadaPor,
                                 Instant atestadaEmUtc) {
        this.id = id;
        this.assento = assento;
        this.campo = campo;
        this.valorOriginal = valorOriginal;
        this.valorCorrigido = valorCorrigido;
        this.justificativa = justificativa;
        this.atestadaPor = atestadaPor;
        this.atestadaEmUtc = atestadaEmUtc;
    }

    public UUID getId() {
        return id;
    }

    public String getCampo() {
        return campo;
    }

    public String getValorOriginal() {
        return valorOriginal;
    }

    public String getValorCorrigido() {
        return valorCorrigido;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public String getAtestadaPor() {
        return atestadaPor;
    }

    public Instant getAtestadaEmUtc() {
        return atestadaEmUtc;
    }
}
