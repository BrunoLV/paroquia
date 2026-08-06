package br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade JPA que representa um {@code Projeto} paroquial: nome, descrição, organização
 * responsável, período (opcional) e status do ciclo de vida ({@link ProjetoStatus}). Apesar do
 * nome da tabela ({@code projetos_eventos}, no schema {@code calendario}), esta entidade
 * representa o próprio projeto — não a relação de vínculo entre projeto e evento, que é feita
 * via {@code EventoEntity#getProjetoId()}.
 */
@Entity
@Table(name = "projetos_eventos", schema = "calendario")
public class ProjetoEventoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 2000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private ProjetoStatus status = ProjetoStatus.ABERTO;

    @Column(name = "grupoParoquial_responsavel_id")
    private UUID grupoParoquialResponsavelId;

    @Column(name = "inicio_utc")
    private Instant inicioUtc;

    @Column(name = "fim_utc")
    private Instant fimUtc;

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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public ProjetoStatus getStatus() {
        return status;
    }

    public void setStatus(ProjetoStatus status) {
        this.status = status;
    }

    public UUID getGrupoParoquialResponsavelId() {
        return grupoParoquialResponsavelId;
    }

    public void setGrupoParoquialResponsavelId(UUID grupoParoquialResponsavelId) {
        this.grupoParoquialResponsavelId = grupoParoquialResponsavelId;
    }

    public Instant getInicioUtc() {
        return inicioUtc;
    }

    public void setInicioUtc(Instant inicioUtc) {
        this.inicioUtc = inicioUtc;
    }

    public Instant getFimUtc() {
        return fimUtc;
    }

    public void setFimUtc(Instant fimUtc) {
        this.fimUtc = fimUtc;
    }
}
