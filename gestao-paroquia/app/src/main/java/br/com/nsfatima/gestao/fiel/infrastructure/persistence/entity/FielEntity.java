package br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.support.infrastructure.crypto.CpfBlindIndex;
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
import jakarta.persistence.Transient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/** Persistência do cadastro civil e comunitário, sem informações sacramentais. */
@Entity
@Table(name = "fieis", schema = "calendario")
@SuppressWarnings("PMD.ExcessivePublicCount")
public class FielEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Convert(converter = EncryptedStringConverter.class)
    private String nome;

    @Convert(converter = EncryptedStringConverter.class)
    private String cpf;

    @Column(name = "cpf_hash", length = 64)
    private String cpfHash;

    @Convert(converter = EncryptedStringConverter.class)
    private String rg;

    @Column(name = "documento_estrangeiro_tipo", length = 16)
    private String documentoEstrangeiroTipo;

    @Column(name = "documento_estrangeiro_numero")
    @Convert(converter = EncryptedStringConverter.class)
    private String documentoEstrangeiroNumero;

    @Column(name = "pais_origem", length = 3)
    private String paisOrigem;

    @Column(name = "data_nascimento")
    @Convert(converter = EncryptedLocalDateConverter.class)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sexo sexo;

    @Convert(converter = EncryptedStringConverter.class)
    private String email;

    @Column(name = "celular")
    @Convert(converter = EncryptedStringConverter.class)
    private String celular;

    @Column(name = "telefone_fixo")
    @Convert(converter = EncryptedStringConverter.class)
    private String telefoneFixo;

    @Column(name = "is_whatsapp", nullable = false)
    private boolean whatsapp;

    @Column(name = "endereco_logradouro")
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoLogradouro;

    @Column(name = "endereco_numero")
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoNumero;

    @Column(name = "endereco_complemento")
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoComplemento;

    @Column(name = "endereco_bairro")
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoBairro;

    @Column(name = "endereco_cep")
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoCep;

    @Column(name = "endereco_cidade")
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoCidade;

    @Column(name = "endereco_estado", length = 2)
    private String enderecoEstado;

    @Column(name = "unidade_territorial_id")
    private UUID unidadeTerritorialId;

    @Column(name = "is_dizimista", nullable = false)
    private boolean dizimista;

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

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "responsavel_menor_nome")
    private String responsavelMenorNome;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "responsavel_menor_cpf", length = 14)
    private String responsavelMenorCpf;

    @Column(name = "uso_imagem_autorizado", nullable = false)
    private boolean usoImagemAutorizado;

    @Column(name = "permitir_divulgacao_aniversario", nullable = false)
    private boolean permitirDivulgacaoAniversario;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "desativado_em_utc")
    private Instant desativadoEmUtc;

    @Column(name = "dados_cadastrais_eliminados", nullable = false)
    private boolean dadosCadastraisEliminados;

    @Transient
    private Vocacao vocacao;

    public UUID getId() {
        return id;
    }

    public void setId(UUID value) {
        id = value;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String value) {
        nome = value;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String value) {
        cpf = digitos(value);
        cpfHash = CpfBlindIndex.hash(cpf);
    }

    public String getCpfHash() {
        return cpfHash;
    }

    public void setCpfHash(String value) {
        cpfHash = value;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String value) {
        rg = value == null ? null : value.trim();
    }

    public String getDocumentoEstrangeiroTipo() {
        return documentoEstrangeiroTipo;
    }

    public void setDocumentoEstrangeiroTipo(String value) {
        documentoEstrangeiroTipo = value;
    }

    public String getDocumentoEstrangeiroNumero() {
        return documentoEstrangeiroNumero;
    }

    public void setDocumentoEstrangeiroNumero(String value) {
        documentoEstrangeiroNumero = digitos(value);
    }

    public String getPaisOrigem() {
        return paisOrigem;
    }

    public void setPaisOrigem(String value) {
        paisOrigem = value;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate value) {
        dataNascimento = value;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo value) {
        sexo = value;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String value) {
        email = value;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String value) {
        celular = digitos(value);
    }

    public String getTelefone() {
        return celular;
    }

    public void setTelefone(String value) {
        setCelular(value);
    }

    public void setEndereco(String value) {
        enderecoLogradouro = value;
    }

    public Vocacao getVocacao() {
        return vocacao;
    }

    public void setVocacao(Vocacao value) {
        vocacao = value;
    }

    public void setDataBatismo(LocalDate value) {
    }

    public void setDataPrimeiraComunhao(LocalDate value) {
    }

    public String getTelefoneFixo() {
        return telefoneFixo;
    }

    public void setTelefoneFixo(String value) {
        telefoneFixo = digitos(value);
    }

    public boolean isWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(boolean value) {
        whatsapp = value;
    }

    public String getEnderecoLogradouro() {
        return enderecoLogradouro;
    }

    public void setEnderecoLogradouro(String value) {
        enderecoLogradouro = value;
    }

    public String getEnderecoNumero() {
        return enderecoNumero;
    }

    public void setEnderecoNumero(String value) {
        enderecoNumero = value;
    }

    public String getEnderecoComplemento() {
        return enderecoComplemento;
    }

    public void setEnderecoComplemento(String value) {
        enderecoComplemento = value;
    }

    public String getEnderecoBairro() {
        return enderecoBairro;
    }

    public void setEnderecoBairro(String value) {
        enderecoBairro = value;
    }

    public String getEnderecoCep() {
        return enderecoCep;
    }

    public void setEnderecoCep(String value) {
        enderecoCep = digitos(value);
    }

    public String getEnderecoCidade() {
        return enderecoCidade;
    }

    public void setEnderecoCidade(String value) {
        enderecoCidade = value;
    }

    public String getEnderecoEstado() {
        return enderecoEstado;
    }

    public void setEnderecoEstado(String value) {
        enderecoEstado = value;
    }

    public UUID getUnidadeTerritorialId() {
        return unidadeTerritorialId;
    }

    public void setUnidadeTerritorialId(UUID value) {
        unidadeTerritorialId = value;
    }

    public boolean isDizimista() {
        return dizimista;
    }

    public void setDizimista(boolean value) {
        dizimista = value;
    }

    public boolean isTermoConsentimentoAceito() {
        return termoConsentimentoAceito;
    }

    public void setTermoConsentimentoAceito(boolean value) {
        termoConsentimentoAceito = value;
    }

    public Instant getDataHoraConsentimentoUtc() {
        return dataHoraConsentimentoUtc;
    }

    public void setDataHoraConsentimentoUtc(Instant value) {
        dataHoraConsentimentoUtc = value;
    }

    public String getIpConsentimento() {
        return ipConsentimento;
    }

    public void setIpConsentimento(String value) {
        ipConsentimento = value;
    }

    public String getVersaoTermoConsentimento() {
        return versaoTermoConsentimento;
    }

    public void setVersaoTermoConsentimento(String value) {
        versaoTermoConsentimento = value;
    }

    public String getCanalConsentimento() {
        return canalConsentimento;
    }

    public void setCanalConsentimento(String value) {
        canalConsentimento = value;
    }

    public String getHashTermoConsentimento() {
        return hashTermoConsentimento;
    }

    public void setHashTermoConsentimento(String value) {
        hashTermoConsentimento = value;
    }

    public String getResponsavelMenorNome() {
        return responsavelMenorNome;
    }

    public void setResponsavelMenorNome(String value) {
        responsavelMenorNome = value;
    }

    public String getResponsavelMenorCpf() {
        return responsavelMenorCpf;
    }

    public void setResponsavelMenorCpf(String value) {
        responsavelMenorCpf = digitos(value);
    }

    public boolean isUsoImagemAutorizado() {
        return usoImagemAutorizado;
    }

    public void setUsoImagemAutorizado(boolean value) {
        usoImagemAutorizado = value;
    }

    public boolean isPermitirDivulgacaoAniversario() {
        return permitirDivulgacaoAniversario;
    }

    public void setPermitirDivulgacaoAniversario(boolean value) {
        permitirDivulgacaoAniversario = value;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean value) {
        ativo = value;
    }

    public Instant getDesativadoEmUtc() {
        return desativadoEmUtc;
    }

    public void setDesativadoEmUtc(Instant value) {
        desativadoEmUtc = value;
    }

    public boolean isDadosCadastraisEliminados() {
        return dadosCadastraisEliminados;
    }

    public void setDadosCadastraisEliminados(boolean value) {
        dadosCadastraisEliminados = value;
    }

    /** Desativação reversível do cadastro (LGPD). */
    public void desativar(Instant agora, String actor) {
        this.ativo = false;
        this.desativadoEmUtc = agora;
    }

    /** Reativa um cadastro desativado; rejeita se os dados civis já foram anonimizados. */
    public void reativar(Instant agora, String actor) {
        if (dadosCadastraisEliminados) {
            throw new IllegalStateException("Cadastro anonimizado não pode ser reativado.");
        }
        this.ativo = true;
        this.desativadoEmUtc = null;
    }

    /**
     * Anonimização definitiva (LGPD): elimina, num único ponto, todos os dados pessoais identificáveis
     * do cadastro. Antes esta lista vivia como sequência de {@code setX(null)} em
     * {@code GerenciarCicloVidaFielUseCase}. Zera também o blind index do CPF, nascimento, sexo e o carimbo
     * de consentimento; dependentes, relações familiares, trilha de consentimento e conta de acesso são
     * eliminados pelo caso de uso (outros agregados/tabelas).
     */
    public void anonimizar(Instant agora, String actor) {
        setNome(null);
        setCpf(null);
        setRg(null);
        setDocumentoEstrangeiroNumero(null);
        setEmail(null);
        setCelular(null);
        setTelefoneFixo(null);
        setEnderecoLogradouro(null);
        setEnderecoNumero(null);
        setEnderecoComplemento(null);
        setEnderecoBairro(null);
        setEnderecoCep(null);
        setEnderecoCidade(null);
        setEnderecoEstado(null);
        setResponsavelMenorNome(null);
        setResponsavelMenorCpf(null);
        this.cpfHash = null;
        this.dataNascimento = null;
        this.sexo = null;
        this.termoConsentimentoAceito = false;
        this.dataHoraConsentimentoUtc = null;
        this.ipConsentimento = null;
        this.usoImagemAutorizado = false;
        this.permitirDivulgacaoAniversario = false;
        this.dadosCadastraisEliminados = true;
    }

    private String digitos(String value) {
        if (value == null) {
            return null;
        }
        return value.replaceAll("\\D", "");
    }
}
