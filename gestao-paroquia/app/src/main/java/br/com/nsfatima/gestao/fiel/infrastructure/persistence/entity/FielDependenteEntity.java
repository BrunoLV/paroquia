package br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.fiel.domain.model.Parentesco;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedLocalDateConverter;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedStringConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fiel_dependentes", schema = "calendario")
public class FielDependenteEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiel_id", nullable = false)
    private FielEntity fiel;

    @Column
    @Convert(converter = EncryptedStringConverter.class)
    private String nome;

    @Column(name = "data_nascimento")
    @Convert(converter = EncryptedLocalDateConverter.class)
    private LocalDate dataNascimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Parentesco parentesco = Parentesco.FILHO;

    @Column(name = "batizado", nullable = false)
    private boolean batizado;

    @Column(name = "primeira_comunhao", nullable = false)
    private boolean primeiraComunhao;

    @Column(name = "crismado", nullable = false)
    private boolean crismado;

    @Column(name = "criado_em_utc", nullable = false)
    private Instant criadoEmUtc = Instant.now();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public FielEntity getFiel() {
        return fiel;
    }

    public void setFiel(FielEntity fiel) {
        this.fiel = fiel;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Parentesco getParentesco() {
        return parentesco;
    }

    public void setParentesco(Parentesco parentesco) {
        this.parentesco = parentesco;
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

    public Instant getCriadoEmUtc() {
        return criadoEmUtc;
    }

    public void setCriadoEmUtc(Instant criadoEmUtc) {
        this.criadoEmUtc = criadoEmUtc;
    }
}
