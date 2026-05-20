package br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "projetos_eventos", schema = "calendario")
public class ProjetoEventoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 2000)
    private String descricao;

    @Column(nullable = false, length = 32)
    private String status = ProjetoStatus.ATIVO.name();

    @Column(name = "organizacao_responsavel_id")
    private UUID organizacaoResponsavelId;

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

    public String getStatus() {
        return status;
    }

    public ProjetoStatus getStatusEnum() {
        return ProjetoStatus.fromJson(status);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus(ProjetoStatus status) {
        this.status = status == null ? null : status.name();
    }

    public UUID getOrganizacaoResponsavelId() {
        return organizacaoResponsavelId;
    }

    public void setOrganizacaoResponsavelId(UUID organizacaoResponsavelId) {
        this.organizacaoResponsavelId = organizacaoResponsavelId;
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
