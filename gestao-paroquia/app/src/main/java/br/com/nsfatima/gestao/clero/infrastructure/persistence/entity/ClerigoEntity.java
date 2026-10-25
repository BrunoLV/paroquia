package br.com.nsfatima.gestao.clero.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.clero.domain.model.GrauOrdem;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedLocalDateConverter;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedStringConverter;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Persistência do cadastro pessoal e canônico de um clérigo. */
@Entity
@Table(name = "clerigos", schema = "calendario")
@SuppressWarnings("PMD.ExcessivePublicCount")
public class ClerigoEntity extends BaseAuditableEntity {
    @Id
    private UUID id;

    @Convert(converter = EncryptedStringConverter.class)
    private String nomeCivil;

    @Column(nullable = false)
    private String nomeEclesiastico;

    @Convert(converter = EncryptedLocalDateConverter.class)
    private LocalDate dataNascimento;

    @Column(length = 3, nullable = false)
    private String paisOrigem;

    @Convert(converter = EncryptedStringConverter.class)
    private String cpf;

    @Convert(converter = EncryptedStringConverter.class)
    private String rg;

    @Convert(converter = EncryptedStringConverter.class)
    private String documentoEstrangeiro;

    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Convert(converter = EncryptedStringConverter.class)
    private String celular;

    @Convert(converter = EncryptedStringConverter.class)
    private String telefoneFixo;

    @Column(nullable = false)
    private boolean whatsapp;

    @Convert(converter = EncryptedStringConverter.class)
    private String endereco;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_cep")
    private String enderecoCep;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_logradouro", length = 500)
    private String enderecoLogradouro;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_numero")
    private String enderecoNumero;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_complemento")
    private String enderecoComplemento;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_bairro")
    private String enderecoBairro;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "endereco_cidade")
    private String enderecoCidade;

    @Column(name = "endereco_uf", length = 2)
    private String enderecoUf;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GrauOrdem grauOrdem;

    @Column(nullable = false)
    private String dioceseIncardinacao;

    private LocalDate dataOrdenacaoDiaconal;
    private LocalDate dataOrdenacaoPresbiteral;
    private LocalDate dataOrdenacaoEpiscopal;

    @Column(nullable = false)
    private boolean ativo = true;

    private Instant deletedAt;

    @Column(name = "termo_consentimento_aceito", nullable = false)
    private boolean termoConsentimentoAceito;

    @Column(name = "data_hora_consentimento_utc")
    private Instant dataHoraConsentimentoUtc;

    @Column(name = "ip_consentimento")
    private String ipConsentimento;

    @Column(name = "versao_termo_consentimento", length = 20)
    private String versaoTermoConsentimento = "v1.0";

    @Column(name = "canal_consentimento", length = 30)
    private String canalConsentimento = "PORTAL_AUTOATENDIMENTO";

    @Column(name = "hash_termo_consentimento", length = 64)
    private String hashTermoConsentimento;

    @Column(name = "uso_imagem_autorizado", nullable = false)
    private boolean usoImagemAutorizado;

    @Column(name = "permitir_divulgacao_aniversario", nullable = false)
    private boolean permitirDivulgacaoAniversario;

    public UUID getId() {
        return id;
    }

    public void setId(UUID valor) {
        id = valor;
    }

    public String getNomeCivil() {
        return nomeCivil;
    }

    public void setNomeCivil(String valor) {
        nomeCivil = valor;
    }

    public String getNomeEclesiastico() {
        return nomeEclesiastico;
    }

    public void setNomeEclesiastico(String valor) {
        nomeEclesiastico = valor;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate valor) {
        dataNascimento = valor;
    }

    public String getPaisOrigem() {
        return paisOrigem;
    }

    public void setPaisOrigem(String valor) {
        paisOrigem = valor;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String valor) {
        cpf = digitos(valor);
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String valor) {
        rg = digitos(valor);
    }

    public String getDocumentoEstrangeiro() {
        return documentoEstrangeiro;
    }

    public void setDocumentoEstrangeiro(String valor) {
        documentoEstrangeiro = digitos(valor);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String valor) {
        email = valor;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String valor) {
        celular = digitos(valor);
    }

    public String getTelefoneFixo() {
        return telefoneFixo;
    }

    public void setTelefoneFixo(String valor) {
        telefoneFixo = digitos(valor);
    }

    public boolean isWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(boolean valor) {
        whatsapp = valor;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String valor) {
        endereco = valor;
    }

    public String getEnderecoCep() {
        return enderecoCep;
    }

    public void setEnderecoCep(String valor) {
        enderecoCep = valor;
    }

    public String getEnderecoLogradouro() {
        return enderecoLogradouro;
    }

    public void setEnderecoLogradouro(String valor) {
        enderecoLogradouro = valor;
    }

    public String getEnderecoNumero() {
        return enderecoNumero;
    }

    public void setEnderecoNumero(String valor) {
        enderecoNumero = valor;
    }

    public String getEnderecoComplemento() {
        return enderecoComplemento;
    }

    public void setEnderecoComplemento(String valor) {
        enderecoComplemento = valor;
    }

    public String getEnderecoBairro() {
        return enderecoBairro;
    }

    public void setEnderecoBairro(String valor) {
        enderecoBairro = valor;
    }

    public String getEnderecoCidade() {
        return enderecoCidade;
    }

    public void setEnderecoCidade(String valor) {
        enderecoCidade = valor;
    }

    public String getEnderecoUf() {
        return enderecoUf;
    }

    public void setEnderecoUf(String valor) {
        enderecoUf = valor;
    }

    public GrauOrdem getGrauOrdem() {
        return grauOrdem;
    }

    public void setGrauOrdem(GrauOrdem valor) {
        grauOrdem = valor;
    }

    public String getDioceseIncardinacao() {
        return dioceseIncardinacao;
    }

    public void setDioceseIncardinacao(String valor) {
        dioceseIncardinacao = valor;
    }

    public LocalDate getDataOrdenacaoDiaconal() {
        return dataOrdenacaoDiaconal;
    }

    public void setDataOrdenacaoDiaconal(LocalDate valor) {
        dataOrdenacaoDiaconal = valor;
    }

    public LocalDate getDataOrdenacaoPresbiteral() {
        return dataOrdenacaoPresbiteral;
    }

    public void setDataOrdenacaoPresbiteral(LocalDate valor) {
        dataOrdenacaoPresbiteral = valor;
    }

    public LocalDate getDataOrdenacaoEpiscopal() {
        return dataOrdenacaoEpiscopal;
    }

    public void setDataOrdenacaoEpiscopal(LocalDate valor) {
        dataOrdenacaoEpiscopal = valor;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean valor) {
        ativo = valor;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant valor) {
        deletedAt = valor;
    }

    public boolean isTermoConsentimentoAceito() {
        return termoConsentimentoAceito;
    }

    public void setTermoConsentimentoAceito(boolean valor) {
        termoConsentimentoAceito = valor;
    }

    public Instant getDataHoraConsentimentoUtc() {
        return dataHoraConsentimentoUtc;
    }

    public void setDataHoraConsentimentoUtc(Instant valor) {
        dataHoraConsentimentoUtc = valor;
    }

    public String getIpConsentimento() {
        return ipConsentimento;
    }

    public void setIpConsentimento(String valor) {
        ipConsentimento = valor;
    }

    public String getVersaoTermoConsentimento() {
        return versaoTermoConsentimento;
    }

    public void setVersaoTermoConsentimento(String valor) {
        versaoTermoConsentimento = valor;
    }

    public String getCanalConsentimento() {
        return canalConsentimento;
    }

    public void setCanalConsentimento(String valor) {
        canalConsentimento = valor;
    }

    public String getHashTermoConsentimento() {
        return hashTermoConsentimento;
    }

    public void setHashTermoConsentimento(String valor) {
        hashTermoConsentimento = valor;
    }

    public boolean isUsoImagemAutorizado() {
        return usoImagemAutorizado;
    }

    public void setUsoImagemAutorizado(boolean valor) {
        usoImagemAutorizado = valor;
    }

    public boolean isPermitirDivulgacaoAniversario() {
        return permitirDivulgacaoAniversario;
    }

    public void setPermitirDivulgacaoAniversario(boolean valor) {
        permitirDivulgacaoAniversario = valor;
    }

    private String digitos(String valor) {
        if (valor == null) {
            return null;
        }
        return valor.replaceAll("\\D", "");
    }
}
