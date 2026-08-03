package br.com.nsfatima.gestao.calendario.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Vincula as propriedades de configuração sob o prefixo {@code app.paroquia}, fornecendo os
 * valores padrão de identidade visual e institucional da paróquia (nome, diocese, endereço,
 * cores e caminho do logo) usados como fallback por {@link ParoquiaConfiguracaoInitializer} ao
 * semear a configuração persistida no banco.
 */
@Component
@ConfigurationProperties(prefix = "app.paroquia")
public class ParoquiaProperties {

    private String nome = "Paróquia Nossa Senhora de Fátima";
    private String diocese = "Diocese de São Miguel Paulista";
    private String endereco = "Rua Gijoca, 7 - Vila Curuçá Velha - São Paulo/SP";
    private String corPrimariaHex = "#1A237E";
    private String corSecundariaHex = "#808080";
    private String logoPath = "/logo-paroquia.png";

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

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }
}
