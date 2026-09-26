package br.com.nsfatima.gestao.clero.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade JPA para persistência dos dados de Governo Eclesiástico da Paróquia.
 */
@Entity
@Table(name = "governo_eclesiastico", schema = "calendario")
public class GovernoEclesiasticoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(name = "diocese_id", nullable = false)
    private UUID dioceseId;

    @Column(name = "paroco_id")
    private UUID parocoId;

    @Column(name = "data_posse_paroco")
    private LocalDate dataPosseParoco;

    @Column(name = "decreto_erecao_paroquia", length = 100)
    private String decretoErecaoParoquia;

    @Column(name = "data_instalacao_paroquia")
    private LocalDate dataInstalacaoParoquia;

    @Column(name = "livro_tombo_numero", length = 50)
    private String livroTomboNumero;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getDioceseId() {
        return dioceseId;
    }

    public void setDioceseId(UUID dioceseId) {
        this.dioceseId = dioceseId;
    }

    public UUID getParocoId() {
        return parocoId;
    }

    public void setParocoId(UUID parocoId) {
        this.parocoId = parocoId;
    }

    public LocalDate getDataPosseParoco() {
        return dataPosseParoco;
    }

    public void setDataPosseParoco(LocalDate dataPosseParoco) {
        this.dataPosseParoco = dataPosseParoco;
    }

    public String getDecretoErecaoParoquia() {
        return decretoErecaoParoquia;
    }

    public void setDecretoErecaoParoquia(String decretoErecaoParoquia) {
        this.decretoErecaoParoquia = decretoErecaoParoquia;
    }

    public LocalDate getDataInstalacaoParoquia() {
        return dataInstalacaoParoquia;
    }

    public void setDataInstalacaoParoquia(LocalDate dataInstalacaoParoquia) {
        this.dataInstalacaoParoquia = dataInstalacaoParoquia;
    }

    public String getLivroTomboNumero() {
        return livroTomboNumero;
    }

    public void setLivroTomboNumero(String livroTomboNumero) {
        this.livroTomboNumero = livroTomboNumero;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
