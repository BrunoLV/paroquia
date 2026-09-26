package br.com.nsfatima.gestao.organizacao.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Agregado Raiz representando uma Unidade Territorial física da paróquia (Igreja Matriz ou Capela/Comunidade).
 * Contém dados canônicos, civis, prediais e regulatórios.
 */
public class UnidadeTerritorial {
    private final UUID id;
    private String nome;
    private TipoUnidadeTerritorial tipo;
    private boolean ativo;
    private UUID parentId;

    // Canônico
    private String padroeiro;
    private String dataFestaPadroeira;
    private LocalDate dataDedicacaoAltar;
    private boolean possuiSacrario;
    private boolean possuiPiaBatismal;
    private UUID clerigoResponsavelId;

    // Fiscal e Imobiliário
    private String cnpj;
    private String inscricaoMunicipal;
    private String inscricaoImobiliariaIptu;
    private String processoImunidadeIptu;
    private String cartorioRgi;
    private String matriculaImovel;
    private String tipoAquisicao;
    private BigDecimal areaTerrenoM2;
    private BigDecimal areaConstruidaM2;

    // Segurança e Concessionárias
    private String avcbNumero;
    private LocalDate avcbValidade;
    private Integer lotacaoMaximaBombeiros;
    private String codigoConsumidorEnergia;
    private String codigoConsumidorAgua;

    // Contato e Endereço
    private String telefone;
    private String email;
    private EnderecoUnidade endereco;

    /** Construtor para compatibilidade legada. */
    public UnidadeTerritorial(UUID id, String nome, TipoUnidadeTerritorial tipo, String contato,
                              boolean ativo, UUID parentId) {
        this.id = Objects.requireNonNull(id, "ID da unidade é obrigatório.");
        this.nome = Objects.requireNonNull(nome, "Nome da unidade é obrigatório.").trim();
        this.tipo = Objects.requireNonNull(tipo, "Tipo da unidade é obrigatório.");
        this.telefone = contato != null ? contato.trim() : null;
        this.ativo = ativo;
        this.parentId = parentId;
        this.possuiSacrario = true;
        this.possuiPiaBatismal = (tipo == TipoUnidadeTerritorial.MATRIZ);
        this.endereco = EnderecoUnidade.vazio();
        validarInvariantes();
    }

    /** Construtor completo com todos os metadados canônicos, civis e regulatórios. */
    public UnidadeTerritorial(UUID id, String nome, TipoUnidadeTerritorial tipo, boolean ativo, UUID parentId,
                              String padroeiro, String dataFestaPadroeira, LocalDate dataDedicacaoAltar,
                              boolean possuiSacrario, boolean possuiPiaBatismal, UUID clerigoResponsavelId,
                              String cnpj, String inscricaoMunicipal, String inscricaoImobiliariaIptu,
                              String processoImunidadeIptu, String cartorioRgi, String matriculaImovel,
                              String tipoAquisicao, BigDecimal areaTerrenoM2, BigDecimal areaConstruidaM2,
                              String avcbNumero, LocalDate avcbValidade, Integer lotacaoMaximaBombeiros,
                              String codigoConsumidorEnergia, String codigoConsumidorAgua,
                              String telefone, String email, EnderecoUnidade endereco) {
        this.id = Objects.requireNonNull(id, "ID da unidade é obrigatório.");
        this.nome = Objects.requireNonNull(nome, "Nome da unidade é obrigatório.").trim();
        this.tipo = Objects.requireNonNull(tipo, "Tipo da unidade é obrigatório.");
        this.ativo = ativo;
        this.parentId = parentId;
        this.padroeiro = padroeiro != null ? padroeiro.trim() : null;
        this.dataFestaPadroeira = dataFestaPadroeira != null ? dataFestaPadroeira.trim() : null;
        this.dataDedicacaoAltar = dataDedicacaoAltar;
        this.possuiSacrario = possuiSacrario;
        this.possuiPiaBatismal = possuiPiaBatismal;
        this.clerigoResponsavelId = clerigoResponsavelId;
        this.cnpj = cnpj != null ? cnpj.trim() : null;
        this.inscricaoMunicipal = inscricaoMunicipal != null ? inscricaoMunicipal.trim() : null;
        this.inscricaoImobiliariaIptu = inscricaoImobiliariaIptu != null ? inscricaoImobiliariaIptu.trim() : null;
        this.processoImunidadeIptu = processoImunidadeIptu != null ? processoImunidadeIptu.trim() : null;
        this.cartorioRgi = cartorioRgi != null ? cartorioRgi.trim() : null;
        this.matriculaImovel = matriculaImovel != null ? matriculaImovel.trim() : null;
        this.tipoAquisicao = tipoAquisicao != null ? tipoAquisicao.trim() : null;
        this.areaTerrenoM2 = areaTerrenoM2;
        this.areaConstruidaM2 = areaConstruidaM2;
        this.avcbNumero = avcbNumero != null ? avcbNumero.trim() : null;
        this.avcbValidade = avcbValidade;
        this.lotacaoMaximaBombeiros = lotacaoMaximaBombeiros;
        this.codigoConsumidorEnergia = codigoConsumidorEnergia != null ? codigoConsumidorEnergia.trim() : null;
        this.codigoConsumidorAgua = codigoConsumidorAgua != null ? codigoConsumidorAgua.trim() : null;
        this.telefone = telefone != null ? telefone.trim() : null;
        this.email = email != null ? email.trim() : null;
        this.endereco = endereco != null ? endereco : EnderecoUnidade.vazio();
        validarInvariantes();
    }

    public void atualizar(String nome, TipoUnidadeTerritorial tipo, boolean ativo, UUID parentId,
                          String padroeiro, String dataFestaPadroeira, LocalDate dataDedicacaoAltar,
                          boolean possuiSacrario, boolean possuiPiaBatismal, UUID clerigoResponsavelId,
                          String cnpj, String inscricaoMunicipal, String inscricaoImobiliariaIptu,
                          String processoImunidadeIptu, String cartorioRgi, String matriculaImovel,
                          String tipoAquisicao, BigDecimal areaTerrenoM2, BigDecimal areaConstruidaM2,
                          String avcbNumero, LocalDate avcbValidade, Integer lotacaoMaximaBombeiros,
                          String codigoConsumidorEnergia, String codigoConsumidorAgua,
                          String telefone, String email, EnderecoUnidade endereco) {
        this.nome = Objects.requireNonNull(nome, "Nome da unidade é obrigatório.").trim();
        this.tipo = Objects.requireNonNull(tipo, "Tipo da unidade é obrigatório.");
        this.ativo = ativo;
        this.parentId = parentId;
        this.padroeiro = padroeiro != null ? padroeiro.trim() : null;
        this.dataFestaPadroeira = dataFestaPadroeira != null ? dataFestaPadroeira.trim() : null;
        this.dataDedicacaoAltar = dataDedicacaoAltar;
        this.possuiSacrario = possuiSacrario;
        this.possuiPiaBatismal = possuiPiaBatismal;
        this.clerigoResponsavelId = clerigoResponsavelId;
        this.cnpj = cnpj != null ? cnpj.trim() : null;
        this.inscricaoMunicipal = inscricaoMunicipal != null ? inscricaoMunicipal.trim() : null;
        this.inscricaoImobiliariaIptu = inscricaoImobiliariaIptu != null ? inscricaoImobiliariaIptu.trim() : null;
        this.processoImunidadeIptu = processoImunidadeIptu != null ? processoImunidadeIptu.trim() : null;
        this.cartorioRgi = cartorioRgi != null ? cartorioRgi.trim() : null;
        this.matriculaImovel = matriculaImovel != null ? matriculaImovel.trim() : null;
        this.tipoAquisicao = tipoAquisicao != null ? tipoAquisicao.trim() : null;
        this.areaTerrenoM2 = areaTerrenoM2;
        this.areaConstruidaM2 = areaConstruidaM2;
        this.avcbNumero = avcbNumero != null ? avcbNumero.trim() : null;
        this.avcbValidade = avcbValidade;
        this.lotacaoMaximaBombeiros = lotacaoMaximaBombeiros;
        this.codigoConsumidorEnergia = codigoConsumidorEnergia != null ? codigoConsumidorEnergia.trim() : null;
        this.codigoConsumidorAgua = codigoConsumidorAgua != null ? codigoConsumidorAgua.trim() : null;
        this.telefone = telefone != null ? telefone.trim() : null;
        this.email = email != null ? email.trim() : null;
        this.endereco = endereco != null ? endereco : EnderecoUnidade.vazio();
        validarInvariantes();
    }

    private void validarInvariantes() {
        if (tipo == TipoUnidadeTerritorial.MATRIZ) {
            this.possuiSacrario = true;
            this.possuiPiaBatismal = true;
        }
        if (parentId != null && parentId.equals(id)) {
            throw new IllegalArgumentException("Uma unidade territorial não pode ser mãe de si mesma.");
        }
    }

    public UUID getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public TipoUnidadeTerritorial getTipo() {
        return tipo;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public UUID getParentId() {
        return parentId;
    }

    public String getPadroeiro() {
        return padroeiro;
    }

    public String getDataFestaPadroeira() {
        return dataFestaPadroeira;
    }

    public LocalDate getDataDedicacaoAltar() {
        return dataDedicacaoAltar;
    }

    public boolean isPossuiSacrario() {
        return possuiSacrario;
    }

    public boolean isPossuiPiaBatismal() {
        return possuiPiaBatismal;
    }

    public UUID getClerigoResponsavelId() {
        return clerigoResponsavelId;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getInscricaoMunicipal() {
        return inscricaoMunicipal;
    }

    public String getInscricaoImobiliariaIptu() {
        return inscricaoImobiliariaIptu;
    }

    public String getProcessoImunidadeIptu() {
        return processoImunidadeIptu;
    }

    public String getCartorioRgi() {
        return cartorioRgi;
    }

    public String getMatriculaImovel() {
        return matriculaImovel;
    }

    public String getTipoAquisicao() {
        return tipoAquisicao;
    }

    public BigDecimal getAreaTerrenoM2() {
        return areaTerrenoM2;
    }

    public BigDecimal getAreaConstruidaM2() {
        return areaConstruidaM2;
    }

    public String getAvcbNumero() {
        return avcbNumero;
    }

    public LocalDate getAvcbValidade() {
        return avcbValidade;
    }

    public Integer getLotacaoMaximaBombeiros() {
        return lotacaoMaximaBombeiros;
    }

    public String getCodigoConsumidorEnergia() {
        return codigoConsumidorEnergia;
    }

    public String getCodigoConsumidorAgua() {
        return codigoConsumidorAgua;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getEmail() {
        return email;
    }

    public EnderecoUnidade getEndereco() {
        return endereco;
    }

    /** Retrocompatibilidade */
    public String getContato() {
        return telefone != null ? telefone : email;
    }
}
