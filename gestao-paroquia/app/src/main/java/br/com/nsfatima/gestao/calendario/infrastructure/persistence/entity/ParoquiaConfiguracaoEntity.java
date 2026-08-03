package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Configuração institucional da paróquia (nome, diocese, endereço, identidade visual),
 * usada para personalizar exportações, relatórios e a apresentação da aplicação. Tipicamente
 * existe uma única instância desta entidade para toda a instalação.
 */
@Entity
@Table(name = "paroquia_configuracao", schema = "calendario")
public class ParoquiaConfiguracaoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private String diocese;

    @Column(nullable = false, length = 500)
    private String endereco;

    /** Bytes da imagem do logotipo da paróquia, usada em relatórios/exportações e na interface. */
    @Column(name = "logo_bytes")
    private byte[] logoBytes;

    /** Cor primária da identidade visual, em hexadecimal (ex.: {@code #1A2B3C}). */
    @Column(name = "cor_primaria_hex", nullable = false, length = 7)
    private String corPrimariaHex;

    /** Cor secundária da identidade visual, em hexadecimal (ex.: {@code #1A2B3C}). */
    @Column(name = "cor_secundaria_hex", nullable = false, length = 7)
    private String corSecundariaHex;

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

    public String getDiocese() {
        return diocese;
    }

    public void setDiocese(String diocese) {
        this.diocese = diocese;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public byte[] getLogoBytes() {
        return logoBytes;
    }

    public void setLogoBytes(byte[] logoBytes) {
        this.logoBytes = logoBytes;
    }

    public String getCorPrimariaHex() {
        return corPrimariaHex;
    }

    public void setCorPrimariaHex(String corPrimariaHex) {
        this.corPrimariaHex = corPrimariaHex;
    }

    public String getCorSecundariaHex() {
        return corSecundariaHex;
    }

    public void setCorSecundariaHex(String corSecundariaHex) {
        this.corSecundariaHex = corSecundariaHex;
    }
}
