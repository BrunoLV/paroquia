package br.com.nsfatima.gestao.iam.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedLocalDateConverter;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedStringConverter;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "usuarios", schema = "calendario")
public class UsuarioEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column
    private String roles;

    @Column(name = "nome")
    @Convert(converter = EncryptedStringConverter.class)
    private String nome;

    @Column(name = "telefone")
    @Convert(converter = EncryptedStringConverter.class)
    private String telefone;

    @Column(name = "data_nascimento")
    @Convert(converter = EncryptedLocalDateConverter.class)
    private LocalDate dataNascimento;

    @Column(name = "igreja_id")
    private UUID igrejaId;

    @Column(name = "batizado", nullable = false)
    private boolean batizado;

    @Column(name = "primeira_comunhao", nullable = false)
    private boolean primeiraComunhao;

    @Column(name = "crismado", nullable = false)
    private boolean crismado;

    @Column(name = "casado_igreja_catolica", nullable = false)
    private boolean casadoIgrejaCatolica;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public UUID getIgrejaId() {
        return igrejaId;
    }

    public void setIgrejaId(UUID igrejaId) {
        this.igrejaId = igrejaId;
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
}

