package br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.governanca.domain.model.TipoTitularConsentimento;
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
 * Entidade para gestão do ciclo de vida de códigos OTP efêmeros de verificação de e-mail.
 */
@Entity
@Table(name = "desafios_consentimento", schema = "calendario")
public class DesafioConsentimentoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "titular_tipo", nullable = false, length = 20)
    private TipoTitularConsentimento titularTipo;

    @Column(name = "titular_id", nullable = false)
    private UUID titularId;

    @Column(name = "codigo_hash", nullable = false, length = 64)
    private String codigoHash;

    @Column(nullable = false)
    private int tentativas;

    @Column(name = "expira_em_utc", nullable = false)
    private Instant expiraEmUtc;

    @Column(name = "validado_em_utc")
    private Instant validadoEmUtc;

    @Column(name = "consumido_em_utc")
    private Instant consumidoEmUtc;

    public Instant getValidadoEmUtc() {
        return validadoEmUtc;
    }

    public void setValidadoEmUtc(Instant validadoEmUtc) {
        this.validadoEmUtc = validadoEmUtc;
    }

    public Instant getConsumidoEmUtc() {
        return consumidoEmUtc;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TipoTitularConsentimento getTitularTipo() {
        return titularTipo;
    }

    public void setTitularTipo(TipoTitularConsentimento titularTipo) {
        this.titularTipo = titularTipo;
    }

    public UUID getTitularId() {
        return titularId;
    }

    public void setTitularId(UUID titularId) {
        this.titularId = titularId;
    }

    public String getCodigoHash() {
        return codigoHash;
    }

    public void setCodigoHash(String codigoHash) {
        this.codigoHash = codigoHash;
    }

    public int getTentativas() {
        return tentativas;
    }

    public void setTentativas(int tentativas) {
        this.tentativas = tentativas;
    }

    public Instant getExpiraEmUtc() {
        return expiraEmUtc;
    }

    public void setExpiraEmUtc(Instant expiraEmUtc) {
        this.expiraEmUtc = expiraEmUtc;
    }
}
