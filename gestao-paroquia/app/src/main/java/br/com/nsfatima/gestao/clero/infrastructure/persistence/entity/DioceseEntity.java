package br.com.nsfatima.gestao.clero.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedStringConverter;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.UUID;

/**
 * Entidade JPA para persistência de Dioceses da Igreja Católica.
 */
@Entity
@Table(name = "dioceses", schema = "calendario")
public class DioceseEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 180, unique = true)
    private String nome;

    @Column(name = "bispo_diocesano", nullable = false, length = 180)
    private String bispoDiocesano;

    @Column(name = "cnpj_mitra", length = 18)
    private String cnpjMitra;

    @Column(length = 100)
    private String cidade;

    @Column(length = 2)
    private String uf;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 500)
    private String contato;

    @Column(nullable = false)
    private boolean ativo = true;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getBispoDiocesano() {
        return bispoDiocesano;
    }

    public void setBispoDiocesano(String bispoDiocesano) {
        this.bispoDiocesano = bispoDiocesano;
    }

    public String getCnpjMitra() {
        return cnpjMitra;
    }

    public void setCnpjMitra(String cnpjMitra) {
        this.cnpjMitra = cnpjMitra;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getContato() {
        return contato;
    }

    public void setContato(String contato) {
        this.contato = contato;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
