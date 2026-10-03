package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.calendario.domain.model.VarianteFundoLogo;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/**
 * Entidade JPA para persistência da identidade visual institucional da paróquia (cores e logotipos
 * binarizados para fundo claro e escuro).
 *
 * <p>Em conformidade com a arquitetura de Fonte Única da Verdade (SSOT), os dados civis e de endereço
 * da paróquia residem na Unidade Territorial do tipo {@code MATRIZ}, enquanto a circunscrição eclesiástica
 * reside em {@code GovernoEclesiastico}/{@code Diocese}. As colunas textuais {@code nome}, {@code diocese}
 * e {@code endereco} são mantidas exclusivamente como legadas/transitórias nesta tabela.
 */
@Entity
@Table(name = "paroquia_configuracao", schema = "calendario")
public class ParoquiaConfiguracaoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    /** @deprecated Os dados civis da paróquia devem ser obtidos da Unidade Territorial MATRIZ. */
    @Deprecated(since = "0.2.0")
    @Column
    private String nome;

    /** @deprecated A diocese canônica deve ser obtida via Governo Eclesiástico. */
    @Deprecated(since = "0.2.0")
    @Column
    private String diocese;

    /** @deprecated O endereço predial deve ser obtido da Unidade Territorial MATRIZ. */
    @Deprecated(since = "0.2.0")
    @Column(length = 500)
    private String endereco;

    /** Bytes da imagem do logotipo legado, mantido sincronizado com {@link #logoFundoClaroBytes}. */
    @Column(name = "logo_bytes")
    private byte[] logoBytes;

    private static final String DEFAULT_PNG_MIME = "image/png";

    /** Bytes binarizados do logotipo oficial da paróquia para aplicação sobre fundos claros. */
    @Column(name = "logo_fundo_claro_bytes")
    private byte[] logoFundoClaroBytes;

    /** Tipo MIME do logotipo para fundo claro (ex.: {@code image/png}). */
    @Column(name = "logo_fundo_claro_tipo_mime", length = 64)
    private String logoFundoClaroTipoMime = DEFAULT_PNG_MIME;

    /** Bytes binarizados do logotipo oficial da paróquia para aplicação sobre fundos escuros. */
    @Column(name = "logo_fundo_escuro_bytes")
    private byte[] logoFundoEscuroBytes;

    /** Tipo MIME do logotipo para fundo escuro (ex.: {@code image/png}). */
    @Column(name = "logo_fundo_escuro_tipo_mime", length = 64)
    private String logoFundoEscuroTipoMime = DEFAULT_PNG_MIME;

    /** Cor primária da identidade visual, em hexadecimal (ex.: {@code #1A2B3C}). */
    @Column(name = "cor_primaria_hex", nullable = false, length = 7)
    private String corPrimariaHex = "#1A237E";

    /** Cor secundária da identidade visual, em hexadecimal (ex.: {@code #1A2B3C}). */
    @Column(name = "cor_secundaria_hex", nullable = false, length = 7)
    private String corSecundariaHex = "#808080";

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
        if (logoFundoClaroBytes != null && logoFundoClaroBytes.length > 0) {
            return logoFundoClaroBytes;
        }
        return logoBytes;
    }

    public void setLogoBytes(byte[] logoBytes) {
        this.logoBytes = logoBytes;
        if (this.logoFundoClaroBytes == null || this.logoFundoClaroBytes.length == 0) {
            this.logoFundoClaroBytes = logoBytes;
        }
    }

    public byte[] getLogoFundoClaroBytes() {
        return logoFundoClaroBytes;
    }

    public void setLogoFundoClaroBytes(byte[] logoFundoClaroBytes) {
        this.logoFundoClaroBytes = logoFundoClaroBytes;
        this.logoBytes = logoFundoClaroBytes;
    }

    public String getLogoFundoClaroTipoMime() {
        return logoFundoClaroTipoMime != null ? logoFundoClaroTipoMime : DEFAULT_PNG_MIME;
    }

    public void setLogoFundoClaroTipoMime(String logoFundoClaroTipoMime) {
        this.logoFundoClaroTipoMime = logoFundoClaroTipoMime;
    }

    public byte[] getLogoFundoEscuroBytes() {
        return logoFundoEscuroBytes;
    }

    public void setLogoFundoEscuroBytes(byte[] logoFundoEscuroBytes) {
        this.logoFundoEscuroBytes = logoFundoEscuroBytes;
    }

    public String getLogoFundoEscuroTipoMime() {
        return logoFundoEscuroTipoMime != null ? logoFundoEscuroTipoMime : DEFAULT_PNG_MIME;
    }

    public void setLogoFundoEscuroTipoMime(String logoFundoEscuroTipoMime) {
        this.logoFundoEscuroTipoMime = logoFundoEscuroTipoMime;
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

    /**
     * Retorna os bytes do logotipo correspondentes à variante especificada, realizando fallback inteligente
     * caso a variante escura ainda não tenha sido cadastrada.
     *
     * @param variante variante desejada (CLARO ou ESCURO)
     * @return bytes da imagem ou null se não houver imagem
     */
    public byte[] getLogoBytesPorVariante(VarianteFundoLogo variante) {
        if (variante == VarianteFundoLogo.ESCURO) {
            if (logoFundoEscuroBytes != null && logoFundoEscuroBytes.length > 0) {
                return logoFundoEscuroBytes;
            }
        }
        return getLogoBytes();
    }

    /**
     * Retorna o tipo MIME correspondente à variante especificada.
     *
     * @param variante variante desejada (CLARO ou ESCURO)
     * @return tipo MIME (ex.: image/png)
     */
    public String getTipoMimePorVariante(VarianteFundoLogo variante) {
        if (variante == VarianteFundoLogo.ESCURO && logoFundoEscuroBytes != null && logoFundoEscuroBytes.length > 0) {
            return getLogoFundoEscuroTipoMime();
        }
        return getLogoFundoClaroTipoMime();
    }
}
