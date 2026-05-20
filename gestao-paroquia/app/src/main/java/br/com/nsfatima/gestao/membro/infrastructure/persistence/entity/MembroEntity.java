package br.com.nsfatima.gestao.membro.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "membros", schema = "calendario")
public class MembroEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "nome_completo", nullable = false, length = 255)
    private String nomeCompleto;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(length = 255)
    private String email;

    @Column(length = 20)
    private String telefone;

    @Column(length = 500)
    private String endereco;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(nullable = false)
    private boolean ativo = true;

    // Campos sacramentais
    @Column(name = "data_batismo")
    private LocalDate dataBatismo;

    @Column(name = "local_batismo", length = 255)
    private String localBatismo;

    @Column(name = "data_crisma")
    private LocalDate dataCrisma;

    @Column(name = "data_matrimonio")
    private LocalDate dataMatrimonio;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public void setNomeCompleto(String nomeCompleto) {
        this.nomeCompleto = nomeCompleto;
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

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDate getDataBatismo() {
        return dataBatismo;
    }

    public void setDataBatismo(LocalDate dataBatismo) {
        this.dataBatismo = dataBatismo;
    }

    public String getLocalBatismo() {
        return localBatismo;
    }

    public void setLocalBatismo(String localBatismo) {
        this.localBatismo = localBatismo;
    }

    public LocalDate getDataCrisma() {
        return dataCrisma;
    }

    public void setDataCrisma(LocalDate dataCrisma) {
        this.dataCrisma = dataCrisma;
    }

    public LocalDate getDataMatrimonio() {
        return dataMatrimonio;
    }

    public void setDataMatrimonio(LocalDate dataMatrimonio) {
        this.dataMatrimonio = dataMatrimonio;
    }
}
