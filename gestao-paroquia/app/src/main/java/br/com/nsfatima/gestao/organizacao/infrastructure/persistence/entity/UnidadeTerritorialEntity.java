package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoUnidadeTerritorial;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedStringConverter;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade JPA que persiste um nó da hierarquia territorial e predial da paróquia (Matriz ou Comunidade).
 * Dados PII de contato e endereço são protegidos por criptografia em repouso conforme a LGPD.
 */
@Entity
@Table(name = "unidade_territorial", schema = "calendario")
@SuppressWarnings("PMD.ExcessivePublicCount")
public class UnidadeTerritorialEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TipoUnidadeTerritorial tipo;

    @Column
    private String contato;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "parent_id")
    private UUID parentId;

    // --- Dados Canônicos ---

    @Column(length = 150)
    private String padroeiro;

    @Column(name = "data_festa_padroeira", length = 5)
    private String dataFestaPadroeira;

    @Column(name = "data_dedicacao_altar")
    private LocalDate dataDedicacaoAltar;

    @Column(name = "possui_sacrario", nullable = false)
    private boolean possuiSacrario = true;

    @Column(name = "possui_pia_batismal", nullable = false)
    private boolean possuiPiaBatismal;

    @Column(name = "clerigo_responsavel_id")
    private UUID clerigoResponsavelId;

    // --- Dados Fiscais e Jurídicos ---

    @Column(length = 18)
    private String cnpj;

    @Column(name = "inscricao_municipal", length = 30)
    private String inscricaoMunicipal;

    @Column(name = "inscricao_imobiliaria_iptu", length = 50)
    private String inscricaoImobiliariaIptu;

    @Column(name = "processo_imunidade_iptu", length = 50)
    private String processoImunidadeIptu;

    // --- Registro de Imóveis (RGI) ---

    @Column(name = "cartorio_rgi", length = 120)
    private String cartorioRgi;

    @Column(name = "matricula_imovel", length = 50)
    private String matriculaImovel;

    @Column(name = "tipo_aquisicao", length = 30)
    private String tipoAquisicao;

    @Column(name = "area_terreno_m2", precision = 10, scale = 2)
    private BigDecimal areaTerrenoM2;

    @Column(name = "area_construida_m2", precision = 10, scale = 2)
    private BigDecimal areaConstruidaM2;

    // --- Regularidade e Bombeiros ---

    @Column(name = "avcb_numero", length = 50)
    private String avcbNumero;

    @Column(name = "avcb_validade")
    private LocalDate avcbValidade;

    @Column(name = "lotacao_maxima_bombeiros")
    private Integer lotacaoMaximaBombeiros;

    // --- Concessionárias ---

    @Column(name = "codigo_consumidor_energia", length = 50)
    private String codigoConsumidorEnergia;

    @Column(name = "codigo_consumidor_agua", length = 50)
    private String codigoConsumidorAgua;

    // --- Dados PII Criptografados em Repouso (LGPD) ---

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 500)
    private String telefone;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(length = 500)
    private String email;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_cep", length = 255)
    private String enderecoCep;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_logradouro", length = 500)
    private String enderecoLogradouro;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_numero", length = 255)
    private String enderecoNumero;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_complemento", length = 255)
    private String enderecoComplemento;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_bairro", length = 255)
    private String enderecoBairro;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_cidade", length = 255)
    private String enderecoCidade;

    @Column(name = "endereco_uf", length = 2)
    private String enderecoUf;

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

    public TipoUnidadeTerritorial getTipo() {
        return tipo;
    }

    public void setTipo(TipoUnidadeTerritorial tipo) {
        this.tipo = tipo;
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

    public UUID getParentId() {
        return parentId;
    }

    public void setParentId(UUID parentId) {
        this.parentId = parentId;
    }

    public String getPadroeiro() {
        return padroeiro;
    }

    public void setPadroeiro(String padroeiro) {
        this.padroeiro = padroeiro;
    }

    public String getDataFestaPadroeira() {
        return dataFestaPadroeira;
    }

    public void setDataFestaPadroeira(String dataFestaPadroeira) {
        this.dataFestaPadroeira = dataFestaPadroeira;
    }

    public LocalDate getDataDedicacaoAltar() {
        return dataDedicacaoAltar;
    }

    public void setDataDedicacaoAltar(LocalDate dataDedicacaoAltar) {
        this.dataDedicacaoAltar = dataDedicacaoAltar;
    }

    public boolean isPossuiSacrario() {
        return possuiSacrario;
    }

    public void setPossuiSacrario(boolean possuiSacrario) {
        this.possuiSacrario = possuiSacrario;
    }

    public boolean isPossuiPiaBatismal() {
        return possuiPiaBatismal;
    }

    public void setPossuiPiaBatismal(boolean possuiPiaBatismal) {
        this.possuiPiaBatismal = possuiPiaBatismal;
    }

    public UUID getClerigoResponsavelId() {
        return clerigoResponsavelId;
    }

    public void setClerigoResponsavelId(UUID clerigoResponsavelId) {
        this.clerigoResponsavelId = clerigoResponsavelId;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getInscricaoMunicipal() {
        return inscricaoMunicipal;
    }

    public void setInscricaoMunicipal(String inscricaoMunicipal) {
        this.inscricaoMunicipal = inscricaoMunicipal;
    }

    public String getInscricaoImobiliariaIptu() {
        return inscricaoImobiliariaIptu;
    }

    public void setInscricaoImobiliariaIptu(String inscricaoImobiliariaIptu) {
        this.inscricaoImobiliariaIptu = inscricaoImobiliariaIptu;
    }

    public String getProcessoImunidadeIptu() {
        return processoImunidadeIptu;
    }

    public void setProcessoImunidadeIptu(String processoImunidadeIptu) {
        this.processoImunidadeIptu = processoImunidadeIptu;
    }

    public String getCartorioRgi() {
        return cartorioRgi;
    }

    public void setCartorioRgi(String cartorioRgi) {
        this.cartorioRgi = cartorioRgi;
    }

    public String getMatriculaImovel() {
        return matriculaImovel;
    }

    public void setMatriculaImovel(String matriculaImovel) {
        this.matriculaImovel = matriculaImovel;
    }

    public String getTipoAquisicao() {
        return tipoAquisicao;
    }

    public void setTipoAquisicao(String tipoAquisicao) {
        this.tipoAquisicao = tipoAquisicao;
    }

    public BigDecimal getAreaTerrenoM2() {
        return areaTerrenoM2;
    }

    public void setAreaTerrenoM2(BigDecimal areaTerrenoM2) {
        this.areaTerrenoM2 = areaTerrenoM2;
    }

    public BigDecimal getAreaConstruidaM2() {
        return areaConstruidaM2;
    }

    public void setAreaConstruidaM2(BigDecimal areaConstruidaM2) {
        this.areaConstruidaM2 = areaConstruidaM2;
    }

    public String getAvcbNumero() {
        return avcbNumero;
    }

    public void setAvcbNumero(String avcbNumero) {
        this.avcbNumero = avcbNumero;
    }

    public LocalDate getAvcbValidade() {
        return avcbValidade;
    }

    public void setAvcbValidade(LocalDate avcbValidade) {
        this.avcbValidade = avcbValidade;
    }

    public Integer getLotacaoMaximaBombeiros() {
        return lotacaoMaximaBombeiros;
    }

    public void setLotacaoMaximaBombeiros(Integer lotacaoMaximaBombeiros) {
        this.lotacaoMaximaBombeiros = lotacaoMaximaBombeiros;
    }

    public String getCodigoConsumidorEnergia() {
        return codigoConsumidorEnergia;
    }

    public void setCodigoConsumidorEnergia(String codigoConsumidorEnergia) {
        this.codigoConsumidorEnergia = codigoConsumidorEnergia;
    }

    public String getCodigoConsumidorAgua() {
        return codigoConsumidorAgua;
    }

    public void setCodigoConsumidorAgua(String codigoConsumidorAgua) {
        this.codigoConsumidorAgua = codigoConsumidorAgua;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEnderecoCep() {
        return enderecoCep;
    }

    public void setEnderecoCep(String enderecoCep) {
        this.enderecoCep = enderecoCep;
    }

    public String getEnderecoLogradouro() {
        return enderecoLogradouro;
    }

    public void setEnderecoLogradouro(String enderecoLogradouro) {
        this.enderecoLogradouro = enderecoLogradouro;
    }

    public String getEnderecoNumero() {
        return enderecoNumero;
    }

    public void setEnderecoNumero(String enderecoNumero) {
        this.enderecoNumero = enderecoNumero;
    }

    public String getEnderecoComplemento() {
        return enderecoComplemento;
    }

    public void setEnderecoComplemento(String enderecoComplemento) {
        this.enderecoComplemento = enderecoComplemento;
    }

    public String getEnderecoBairro() {
        return enderecoBairro;
    }

    public void setEnderecoBairro(String enderecoBairro) {
        this.enderecoBairro = enderecoBairro;
    }

    public String getEnderecoCidade() {
        return enderecoCidade;
    }

    public void setEnderecoCidade(String enderecoCidade) {
        this.enderecoCidade = enderecoCidade;
    }

    public String getEnderecoUf() {
        return enderecoUf;
    }

    public void setEnderecoUf(String enderecoUf) {
        this.enderecoUf = enderecoUf;
    }
}
