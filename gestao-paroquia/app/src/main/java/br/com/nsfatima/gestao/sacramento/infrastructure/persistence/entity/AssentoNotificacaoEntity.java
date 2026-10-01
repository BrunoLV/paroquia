package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

/** Entidade JPA de {@code calendario.assento_notificacoes}: notificação canônica ao pároco do batismo. */
@Entity
@Table(name = "assento_notificacoes", schema = "calendario")
public class AssentoNotificacaoEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assento_id", nullable = false)
    private AssentoSacramentalEntity assento;

    @Column(name = "destinatario_paroquia", nullable = false)
    private String destinatarioParoquia;

    @Column(name = "emitida_em", nullable = false)
    private LocalDate emitidaEm;

    @Column(name = "enviada_em")
    private LocalDate enviadaEm;

    @Column(name = "registrada_por", length = 128)
    private String registradaPor;

    public AssentoNotificacaoEntity() {
    }

    public AssentoNotificacaoEntity(UUID id, AssentoSacramentalEntity assento, String destinatarioParoquia,
                                    LocalDate emitidaEm, LocalDate enviadaEm, String registradaPor) {
        this.id = id;
        this.assento = assento;
        this.destinatarioParoquia = destinatarioParoquia;
        this.emitidaEm = emitidaEm;
        this.enviadaEm = enviadaEm;
        this.registradaPor = registradaPor;
    }

    public UUID getId() {
        return id;
    }

    public String getDestinatarioParoquia() {
        return destinatarioParoquia;
    }

    public LocalDate getEmitidaEm() {
        return emitidaEm;
    }

    public LocalDate getEnviadaEm() {
        return enviadaEm;
    }

    public String getRegistradaPor() {
        return registradaPor;
    }
}
