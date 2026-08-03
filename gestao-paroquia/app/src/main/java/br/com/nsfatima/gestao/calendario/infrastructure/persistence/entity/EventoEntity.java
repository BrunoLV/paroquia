package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "eventos", schema = "calendario")
public class EventoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(length = 4000)
    private String descricao;

    @Column(name = "organizacao_responsavel_id", nullable = false)
    private UUID organizacaoResponsavelId;

    @Column(name = "inicio_utc", nullable = false)
    private Instant inicioUtc;

    @Column(name = "fim_utc", nullable = false)
    private Instant fimUtc;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "cancelado_motivo", length = 2000)
    private String canceladoMotivo;

    @Column(name = "adicionado_extra_justificativa", length = 4000)
    private String adicionadoExtraJustificativa;

    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_state", length = 32)
    private EstadoConflitoAgenda estadoConflito;

    @Column(name = "conflict_reason", length = 2000)
    private String motivoConflito;

    @Column(name = "recorrencia_id")
    private UUID recorrenciaId;

    @Column(name = "projeto_id")
    private UUID projetoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 32)
    private CategoriaEvento categoria;

    @Column(name = "ano_liturgico_ref")
    private Integer anoLiturgicoRef;

    @Column(name = "ciclo_liturgico_id")
    private UUID cicloLiturgicoId;

    @Column(name = "local_id")
    private UUID localId;

    @Column(name = "igreja_vinculo_id")
    private UUID igrejaVinculoId;

    @Column(name = "nivel_prioridade", nullable = false)
    private Integer nivelPrioridade = 4;

    @Column(name = "autorizado_pelo_cpp", nullable = false)
    private Boolean autorizadoPeloCpp = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "setor_pastoral", length = 64)
    private SetorPastoral setorPastoral;


    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id", insertable = false, updatable = false)
    private List<EventoEnvolvidoEntity> envolvidos;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCanceladoMotivo() {
        return canceladoMotivo;
    }

    public void setCanceladoMotivo(String canceladoMotivo) {
        this.canceladoMotivo = canceladoMotivo;
    }

    public String getAdicionadoExtraJustificativa() {
        return adicionadoExtraJustificativa;
    }

    public void setAdicionadoExtraJustificativa(String adicionadoExtraJustificativa) {
        this.adicionadoExtraJustificativa = adicionadoExtraJustificativa;
    }

    public EstadoConflitoAgenda getEstadoConflito() {
        return estadoConflito;
    }

    public void setEstadoConflito(EstadoConflitoAgenda estadoConflito) {
        this.estadoConflito = estadoConflito;
    }

    public String getMotivoConflito() {
        return motivoConflito;
    }

    public void setMotivoConflito(String motivoConflito) {
        this.motivoConflito = motivoConflito;
    }

    public UUID getRecorrenciaId() {
        return recorrenciaId;
    }

    public void setRecorrenciaId(UUID recorrenciaId) {
        this.recorrenciaId = recorrenciaId;
    }

    public UUID getProjetoId() {
        return projetoId;
    }

    public void setProjetoId(UUID projetoId) {
        this.projetoId = projetoId;
    }

    public CategoriaEvento getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaEvento categoria) {
        this.categoria = categoria;
    }

    public UUID getLocalId() {
        return localId;
    }

    public void setLocalId(UUID localId) {
        this.localId = localId;
    }

    public List<EventoEnvolvidoEntity> getEnvolvidos() {
        return envolvidos;
    }

    public void setEnvolvidos(List<EventoEnvolvidoEntity> envolvidos) {
        this.envolvidos = envolvidos;
    }

    public UUID getIgrejaVinculoId() {
        return igrejaVinculoId;
    }

    public void setIgrejaVinculoId(UUID igrejaVinculoId) {
        this.igrejaVinculoId = igrejaVinculoId;
    }

    public Integer getNivelPrioridade() {
        return nivelPrioridade;
    }

    public void setNivelPrioridade(Integer nivelPrioridade) {
        this.nivelPrioridade = nivelPrioridade;
    }

    public Boolean getAutorizadoPeloCpp() {
        return autorizadoPeloCpp;
    }

    public void setAutorizadoPeloCpp(Boolean autorizadoPeloCpp) {
        this.autorizadoPeloCpp = autorizadoPeloCpp;
    }

    public SetorPastoral getSetorPastoral() {
        return setorPastoral;
    }

    public void setSetorPastoral(SetorPastoral setorPastoral) {
        this.setorPastoral = setorPastoral;
    }

    public Integer getAnoLiturgicoRef() {
        return anoLiturgicoRef;
    }

    public void setAnoLiturgicoRef(Integer anoLiturgicoRef) {
        this.anoLiturgicoRef = anoLiturgicoRef;
    }

    public UUID getCicloLiturgicoId() {
        return cicloLiturgicoId;
    }

    public void setCicloLiturgicoId(UUID cicloLiturgicoId) {
        this.cicloLiturgicoId = cicloLiturgicoId;
    }
}

