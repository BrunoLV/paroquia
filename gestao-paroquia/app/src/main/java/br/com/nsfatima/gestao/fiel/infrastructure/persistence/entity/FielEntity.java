package br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.fiel.domain.model.Vocacao;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedLocalDateConverter;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedStringConverter;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HexFormat;
import java.util.UUID;

/**
 * Registro persistente do agregado {@code Fiel} (pessoa física). Campos com dado pessoal sensível
 * ({@code nome}, {@code cpf}, {@code telefone}, {@code dataNascimento}, {@code endereco}) são armazenados
 * criptografados em coluna via {@link EncryptedStringConverter}/{@link EncryptedLocalDateConverter} (LGPD).
 * <p>
 * A criptografia usada é não-determinística (IV aleatório a cada cifragem), então o mesmo CPF em claro gera
 * um {@code cpf} cifrado diferente a cada gravação — uma constraint {@code UNIQUE} direta sobre essa coluna
 * nunca pegaria duplicidade. Por isso {@code cpfHash} guarda um digest SHA-256 determinístico e
 * não-reversível do CPF em claro, mantido em sincronia automaticamente por {@link #setCpf(String)}, e é essa
 * coluna que carrega a constraint de unicidade real.
 */
@Entity
@Table(name = "fieis", schema = "calendario")
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

    @Column(name = "data_nascimento", nullable = false)
    @Convert(converter = EncryptedLocalDateConverter.class)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String telefone;

    @Column(nullable = false)
    @Convert(converter = EncryptedStringConverter.class)
    private String endereco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Vocacao vocacao;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "batizado", nullable = false)
    private boolean batizado;

    @Column(name = "primeira_comunhao", nullable = false)
    private boolean primeiraComunhao;

    @Column(name = "crismado", nullable = false)
    private boolean crismado;

    @Column(name = "casado_igreja_catolica", nullable = false)
    private boolean casadoIgrejaCatolica;

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

    /**
     * Define o CPF, recalculando automaticamente {@link #cpfHash} para o digest SHA-256 do valor em claro.
     *
     * @param cpf CPF em texto claro; o campo é armazenado criptografado, o hash é derivado antes da
     *            cifragem
     */
    public void setCpf(String cpf) {
        this.cpf = cpf;
        this.cpfHash = hash(cpf);
    }

    public String getCpfHash() {
        return cpfHash;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
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

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
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
        return batizado;
    }

    public void setBatizado(boolean batizado) {
        this.batizado = batizado;
    }

    public boolean isPrimeiraComunhao() {
        return primeiraComunhao;
    }

    public void setPrimeiraComunhao(boolean primeiraComunhao) {
        this.primeiraComunhao = primeiraComunhao;
    }

    public boolean isCrismado() {
        return crismado;
    }

    public void setCrismado(boolean crismado) {
        this.crismado = crismado;
    }

    public boolean isCasadoIgrejaCatolica() {
        return casadoIgrejaCatolica;
    }

    public void setCasadoIgrejaCatolica(boolean casadoIgrejaCatolica) {
        this.casadoIgrejaCatolica = casadoIgrejaCatolica;
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
