package br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.organizacao.domain.model.FuncaoCoordenacao;
import br.com.nsfatima.gestao.organizacao.domain.model.StatusMandato;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entidade JPA que persiste um {@code MandatoCoordenacao}. Referencia organização e fiel apenas por ID, sem
 * navegação JPA, mantendo os agregados independentes.
 */
@Entity
@Table(name = "mandato_coordenacao", schema = "calendario")
public class MandatoCoordenacaoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(name = "organizacao_id", nullable = false)
    private UUID organizacaoId;

    @Column(name = "fiel_id", nullable = false)
    private UUID fielId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FuncaoCoordenacao funcao;

    @Column(nullable = false)
    private LocalDate inicio;

    @Column(nullable = false)
    private LocalDate fim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatusMandato status;

    @Column(name = "homologado_por", length = 128)
    private String homologadoPor;

    @Column(name = "motivo_veto", length = 500)
    private String motivoVeto;

    @Column(name = "decidido_em_utc")
    private Instant decididoEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOrganizacaoId() {
        return organizacaoId;
    }

    public void setOrganizacaoId(UUID organizacaoId) {
        this.organizacaoId = organizacaoId;
    }

    public UUID getFielId() {
        return fielId;
    }

    public void setFielId(UUID fielId) {
        this.fielId = fielId;
    }

    public FuncaoCoordenacao getFuncao() {
        return funcao;
    }

    public void setFuncao(FuncaoCoordenacao funcao) {
        this.funcao = funcao;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public void setFim(LocalDate fim) {
        this.fim = fim;
    }

    public StatusMandato getStatus() {
        return status;
    }

    public void setStatus(StatusMandato status) {
        this.status = status;
    }

    public String getHomologadoPor() {
        return homologadoPor;
    }

    public void setHomologadoPor(String homologadoPor) {
        this.homologadoPor = homologadoPor;
    }

    public String getMotivoVeto() {
        return motivoVeto;
    }

    public void setMotivoVeto(String motivoVeto) {
        this.motivoVeto = motivoVeto;
    }

    public Instant getDecididoEmUtc() {
        return decididoEmUtc;
    }

    public void setDecididoEmUtc(Instant decididoEmUtc) {
        this.decididoEmUtc = decididoEmUtc;
    }
}
