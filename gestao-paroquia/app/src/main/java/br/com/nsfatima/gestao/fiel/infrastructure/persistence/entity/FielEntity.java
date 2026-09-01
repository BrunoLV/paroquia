package br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.fiel.domain.model.EstadoCivil;
import br.com.nsfatima.gestao.fiel.domain.model.PreferenciaCanalComunicacao;
import br.com.nsfatima.gestao.fiel.domain.model.Sexo;
import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedLocalDateConverter;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedStringConverter;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Registro persistente do agregado {@code Fiel} (pessoa física).
 */
@Entity
@Table(name = "fieis", schema = "calendario")
@SuppressWarnings("PMD.ExcessivePublicCount") // entidade JPA com muitos campos (LGPD/vida sacramental): getters/setters são a API esperada, não um sinal de design ruim
public class FielEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String nome;

    @Column(nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String cpf;

    @Column(name = "cpf_hash", nullable = false, length = 64)
    private String cpfHash;

    @Column(name = "rg")
    @Convert(converter = EncryptedStringConverter.class)
    private String rg;

    @Column(name = "data_nascimento", nullable = false)
    @Convert(converter = EncryptedLocalDateConverter.class)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(name = "sexo", length = 20)
    private Sexo sexo = Sexo.NAO_INFORMADO;

    @Column(name = "profissao", length = 100)
    private String profissao;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String telefone;

    @Column(name = "telefone_fixo")
    @Convert(converter = EncryptedStringConverter.class)
    private String telefoneFixo;

    @Column(name = "celular_whatsapp")
    @Convert(converter = EncryptedStringConverter.class)
    private String celularWhatsapp;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferencia_canal_comunicacao", length = 20)
    private PreferenciaCanalComunicacao preferenciaCanalComunicacao = PreferenciaCanalComunicacao.WHATSAPP;

    // Endereço Estruturado (LGPD - Cifrados em repouso)
    @Column(name = "endereco_logradouro", length = 500)
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoLogradouro;

    @Column(name = "endereco_numero", length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoNumero;

    @Column(name = "endereco_complemento", length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoComplemento;

    @Column(name = "endereco_bairro", length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoBairro;

    @Column(name = "endereco_cep", length = 255)
    @Convert(converter = EncryptedStringConverter.class)
    private String enderecoCep;

    @Column(name = "endereco_cidade", length = 255)
    private String enderecoCidade;

    @Column(name = "endereco_estado", length = 2)
    private String enderecoEstado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", length = 30)
    private EstadoCivil estadoCivil = EstadoCivil.SOLTEIRO;

    @Column(name = "conjuge_id")
    private UUID conjugeId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Vocacao vocacao;

    @Column(nullable = false)
    private boolean ativo = true;

    // Vida Sacramental
    @Column(name = "data_batismo")
    @Convert(converter = EncryptedLocalDateConverter.class)
    private LocalDate dataBatismo;

    @Column(name = "paroquia_batismo")
    private String paroquiaBatismo;

    @Column(name = "celebrante_batismo")
    private String celebranteBatismo;

    @Column(name = "livro_batismo", length = 50)
    private String livroBatismo;

    @Column(name = "folha_batismo", length = 50)
    private String folhaBatismo;

    @Column(name = "termo_batismo", length = 50)
    private String termoBatismo;

    @Column(name = "padrinhos_batismo", columnDefinition = "TEXT")
    private String padrinhosBatismo;

    @Column(name = "data_primeira_comunhao")
    @Convert(converter = EncryptedLocalDateConverter.class)
    private LocalDate dataPrimeiraComunhao;

    @Column(name = "paroquia_primeira_comunhao")
    private String paroquiaPrimeiraComunhao;

    @Column(name = "turma_catequese", length = 100)
    private String turmaCatequese;

    @Column(name = "data_crisma")
    @Convert(converter = EncryptedLocalDateConverter.class)
    private LocalDate dataCrisma;

    @Column(name = "paroquia_crisma")
    private String paroquiaCrisma;

    @Column(name = "celebrante_crisma")
    private String celebranteCrisma;

    @Column(name = "padrinho_crisma")
    private String padrinhoCrisma;

    @Column(name = "data_matrimonio")
    @Convert(converter = EncryptedLocalDateConverter.class)
    private LocalDate dataMatrimonio;

    @Column(name = "paroquia_matrimonio")
    private String paroquiaMatrimonio;

    @Column(name = "numero_processo_matrimonial", length = 50)
    private String numeroProcessoMatrimonial;

    @Column(name = "certidao_matrimonio", length = 50)
    private String certidaoMatrimonio;

    @Column(name = "habilidades_voluntariado", columnDefinition = "TEXT")
    private String habilidadesVoluntariado;

    // LGPD Consentimento
    @Column(name = "termo_consentimento_aceito", nullable = false)
    private boolean termoConsentimentoAceito;

    @Column(name = "data_hora_consentimento_utc")
    private Instant dataHoraConsentimentoUtc;

    @Column(name = "ip_consentimento")
    @Convert(converter = EncryptedStringConverter.class)
    private String ipConsentimento;

    @Column(name = "versao_termo_consentimento", length = 20)
    private String versaoTermoConsentimento = "v1.0";

    @Column(name = "uso_imagem_autorizado", nullable = false)
    private boolean usoImagemAutorizado;

    @Column(name = "permitir_divulgacao_aniversario", nullable = false)
    private boolean permitirDivulgacaoAniversario = true;

    @OneToMany(mappedBy = "fiel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FielDependenteEntity> dependentes = new ArrayList<>();

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc;

    @Column(name = "atualizado_em_utc", nullable = false)
    private Instant atualizadoEmUtc;

    @Column(name = "criado_por", nullable = false)
    private String criadoPor;

    @Column(name = "atualizado_por", nullable = false)
    private String atualizadoPor;

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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
        this.cpfHash = hash(cpf);
    }

    public String getCpfHash() {
        return cpfHash;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public String getProfissao() {
        return profissao;
    }

    public void setProfissao(String profissao) {
        this.profissao = profissao;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getTelefoneFixo() {
        return telefoneFixo;
    }

    public void setTelefoneFixo(String telefoneFixo) {
        this.telefoneFixo = telefoneFixo;
    }

    public String getCelularWhatsapp() {
        return celularWhatsapp;
    }

    public void setCelularWhatsapp(String celularWhatsapp) {
        this.celularWhatsapp = celularWhatsapp;
    }

    public PreferenciaCanalComunicacao getPreferenciaCanalComunicacao() {
        return preferenciaCanalComunicacao;
    }

    public void setPreferenciaCanalComunicacao(PreferenciaCanalComunicacao preferenciaCanalComunicacao) {
        this.preferenciaCanalComunicacao = preferenciaCanalComunicacao;
    }

    public String getEndereco() {
        return enderecoLogradouro;
    }

    public void setEndereco(String endereco) {
        this.enderecoLogradouro = endereco;
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

    public String getEnderecoCep() {
        return enderecoCep;
    }

    public void setEnderecoCep(String enderecoCep) {
        this.enderecoCep = enderecoCep;
    }

    public String getEnderecoCidade() {
        return enderecoCidade;
    }

    public void setEnderecoCidade(String enderecoCidade) {
        this.enderecoCidade = enderecoCidade;
    }

    public String getEnderecoEstado() {
        return enderecoEstado;
    }

    public void setEnderecoEstado(String enderecoEstado) {
        this.enderecoEstado = enderecoEstado;
    }

    public EstadoCivil getEstadoCivil() {
        return estadoCivil;
    }

    public void setEstadoCivil(EstadoCivil estadoCivil) {
        this.estadoCivil = estadoCivil;
    }

    public UUID getConjugeId() {
        return conjugeId;
    }

    public void setConjugeId(UUID conjugeId) {
        this.conjugeId = conjugeId;
    }

    public Vocacao getVocacao() {
        return vocacao;
    }

    public void setVocacao(Vocacao vocacao) {
        this.vocacao = vocacao;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isBatizado() {
        return dataBatismo != null || paroquiaBatismo != null || celebranteBatismo != null || livroBatismo != null;
    }

    public boolean isPrimeiraComunhao() {
        return dataPrimeiraComunhao != null || paroquiaPrimeiraComunhao != null || turmaCatequese != null;
    }

    public boolean isCrismado() {
        return dataCrisma != null || paroquiaCrisma != null || celebranteCrisma != null || padrinhoCrisma != null;
    }

    public boolean isCasadoIgrejaCatolica() {
        return dataMatrimonio != null || paroquiaMatrimonio != null
                || numeroProcessoMatrimonial != null || certidaoMatrimonio != null;
    }

    public LocalDate getDataBatismo() {
        return dataBatismo;
    }

    public void setDataBatismo(LocalDate dataBatismo) {
        this.dataBatismo = dataBatismo;
    }

    public String getParoquiaBatismo() {
        return paroquiaBatismo;
    }

    public void setParoquiaBatismo(String paroquiaBatismo) {
        this.paroquiaBatismo = paroquiaBatismo;
    }

    public String getCelebranteBatismo() {
        return celebranteBatismo;
    }

    public void setCelebranteBatismo(String celebranteBatismo) {
        this.celebranteBatismo = celebranteBatismo;
    }

    public String getLivroBatismo() {
        return livroBatismo;
    }

    public void setLivroBatismo(String livroBatismo) {
        this.livroBatismo = livroBatismo;
    }

    public String getFolhaBatismo() {
        return folhaBatismo;
    }

    public void setFolhaBatismo(String folhaBatismo) {
        this.folhaBatismo = folhaBatismo;
    }

    public String getTermoBatismo() {
        return termoBatismo;
    }

    public void setTermoBatismo(String termoBatismo) {
        this.termoBatismo = termoBatismo;
    }

    public String getPadrinhosBatismo() {
        return padrinhosBatismo;
    }

    public void setPadrinhosBatismo(String padrinhosBatismo) {
        this.padrinhosBatismo = padrinhosBatismo;
    }

    public LocalDate getDataPrimeiraComunhao() {
        return dataPrimeiraComunhao;
    }

    public void setDataPrimeiraComunhao(LocalDate dataPrimeiraComunhao) {
        this.dataPrimeiraComunhao = dataPrimeiraComunhao;
    }

    public String getParoquiaPrimeiraComunhao() {
        return paroquiaPrimeiraComunhao;
    }

    public void setParoquiaPrimeiraComunhao(String paroquiaPrimeiraComunhao) {
        this.paroquiaPrimeiraComunhao = paroquiaPrimeiraComunhao;
    }

    public String getTurmaCatequese() {
        return turmaCatequese;
    }

    public void setTurmaCatequese(String turmaCatequese) {
        this.turmaCatequese = turmaCatequese;
    }

    public LocalDate getDataCrisma() {
        return dataCrisma;
    }

    public void setDataCrisma(LocalDate dataCrisma) {
        this.dataCrisma = dataCrisma;
    }

    public String getParoquiaCrisma() {
        return paroquiaCrisma;
    }

    public void setParoquiaCrisma(String paroquiaCrisma) {
        this.paroquiaCrisma = paroquiaCrisma;
    }

    public String getCelebranteCrisma() {
        return celebranteCrisma;
    }

    public void setCelebranteCrisma(String celebranteCrisma) {
        this.celebranteCrisma = celebranteCrisma;
    }

    public String getPadrinhoCrisma() {
        return padrinhoCrisma;
    }

    public void setPadrinhoCrisma(String padrinhoCrisma) {
        this.padrinhoCrisma = padrinhoCrisma;
    }

    public LocalDate getDataMatrimonio() {
        return dataMatrimonio;
    }

    public void setDataMatrimonio(LocalDate dataMatrimonio) {
        this.dataMatrimonio = dataMatrimonio;
    }

    public String getParoquiaMatrimonio() {
        return paroquiaMatrimonio;
    }

    public void setParoquiaMatrimonio(String paroquiaMatrimonio) {
        this.paroquiaMatrimonio = paroquiaMatrimonio;
    }

    public String getNumeroProcessoMatrimonial() {
        return numeroProcessoMatrimonial;
    }

    public void setNumeroProcessoMatrimonial(String numeroProcessoMatrimonial) {
        this.numeroProcessoMatrimonial = numeroProcessoMatrimonial;
    }

    public String getCertidaoMatrimonio() {
        return certidaoMatrimonio;
    }

    public void setCertidaoMatrimonio(String certidaoMatrimonio) {
        this.certidaoMatrimonio = certidaoMatrimonio;
    }

    public String getHabilidadesVoluntariado() {
        return habilidadesVoluntariado;
    }

    public void setHabilidadesVoluntariado(String habilidadesVoluntariado) {
        this.habilidadesVoluntariado = habilidadesVoluntariado;
    }

    public boolean isTermoConsentimentoAceito() {
        return termoConsentimentoAceito;
    }

    public void setTermoConsentimentoAceito(boolean termoConsentimentoAceito) {
        this.termoConsentimentoAceito = termoConsentimentoAceito;
    }

    public Instant getDataHoraConsentimentoUtc() {
        return dataHoraConsentimentoUtc;
    }

    public void setDataHoraConsentimentoUtc(Instant dataHoraConsentimentoUtc) {
        this.dataHoraConsentimentoUtc = dataHoraConsentimentoUtc;
    }

    public String getIpConsentimento() {
        return ipConsentimento;
    }

    public void setIpConsentimento(String ipConsentimento) {
        this.ipConsentimento = ipConsentimento;
    }

    public String getVersaoTermoConsentimento() {
        return versaoTermoConsentimento;
    }

    public void setVersaoTermoConsentimento(String versaoTermoConsentimento) {
        this.versaoTermoConsentimento = versaoTermoConsentimento;
    }

    public boolean isUsoImagemAutorizado() {
        return usoImagemAutorizado;
    }

    public void setUsoImagemAutorizado(boolean usoImagemAutorizado) {
        this.usoImagemAutorizado = usoImagemAutorizado;
    }

    public boolean isPermitirDivulgacaoAniversario() {
        return permitirDivulgacaoAniversario;
    }

    public void setPermitirDivulgacaoAniversario(boolean permitirDivulgacaoAniversario) {
        this.permitirDivulgacaoAniversario = permitirDivulgacaoAniversario;
    }

    public List<FielDependenteEntity> getDependentes() {
        return dependentes;
    }

    public void setDependentes(List<FielDependenteEntity> dependentes) {
        this.dependentes = dependentes;
    }

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant criadoEmUtc) {
        this.criadoEmUtc = criadoEmUtc;
    }

    public Instant getAtualizadoEmUtc() {
        return atualizadoEmUtc;
    }

    public void setAtualizadoEmUtc(Instant atualizadoEmUtc) {
        this.atualizadoEmUtc = atualizadoEmUtc;
    }

    public String getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(String criadoPor) {
        this.criadoPor = criadoPor;
    }

    public String getAtualizadoPor() {
        return atualizadoPor;
    }

    public void setAtualizadoPor(String atualizadoPor) {
        this.atualizadoPor = atualizadoPor;
    }

    private static String hash(String value) {
        if (value == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to hash CPF", e);
        }
    }
}
