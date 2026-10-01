package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.sacramento.domain.model.StatusAssento;
import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Entidade JPA da tabela {@code calendario.assentos_sacramentais}: raiz do assento sacramental com
 * seus participantes, averbações, ressalvas e notificações. A exclusão física é vedada por trigger.
 */
@Entity
@Table(name = "assentos_sacramentais", schema = "calendario")
public class AssentoSacramentalEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 16)
    private TipoSacramento tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private StatusAssento status;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibilidade", nullable = false, length = 16)
    private Visibilidade visibilidade;

    @Column(name = "data_lavratura", nullable = false)
    private LocalDate dataLavratura;

    @Column(name = "lavrado_por", length = 128)
    private String lavradoPor;

    @Column(name = "livro", length = 32)
    private String livro;

    @Column(name = "folha", length = 32)
    private String folha;

    @Column(name = "termo", length = 32)
    private String termo;

    @Column(name = "paroquia_custodiante")
    private String paroquiaCustodiante;

    @Column(name = "data_celebracao")
    private LocalDateTime dataCelebracao;

    @Column(name = "local_celebracao")
    private String localCelebracao;

    @Column(name = "dado_sensivel", nullable = false)
    private boolean dadoSensivel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "dados_especificos", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> dadosEspecificos = new HashMap<>();

    @Column(name = "homologado_por", length = 128)
    private String homologadoPor;

    @Column(name = "homologado_em_utc")
    private Instant homologadoEmUtc;

    @Column(name = "motivo_cancelamento", columnDefinition = "TEXT")
    private String motivoCancelamento;

    @Column(name = "cancelado_por", length = 128)
    private String canceladoPor;

    @Column(name = "cancelado_em_utc")
    private Instant canceladoEmUtc;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @OneToMany(mappedBy = "assento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssentoParticipanteEntity> participantes = new ArrayList<>();

    @OneToMany(mappedBy = "assento", cascade = CascadeType.ALL)
    private List<AverbacaoBatismoEntity> averbacoes = new ArrayList<>();

    @OneToMany(mappedBy = "assento", cascade = CascadeType.ALL)
    private List<AssentoRessalvaEntity> ressalvas = new ArrayList<>();

    @OneToMany(mappedBy = "assento", cascade = CascadeType.ALL)
    private List<AssentoNotificacaoEntity> notificacoes = new ArrayList<>();

    public AssentoSacramentalEntity() {
    }

    public AssentoSacramentalEntity(UUID id) {
        this.id = id;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    public UUID getId() {
        return id;
    }

    public TipoSacramento getTipo() {
        return tipo;
    }

    public void setTipo(TipoSacramento tipo) {
        this.tipo = tipo;
    }

    public StatusAssento getStatus() {
        return status;
    }

    public void setStatus(StatusAssento status) {
        this.status = status;
    }

    public Visibilidade getVisibilidade() {
        return visibilidade;
    }

    public void setVisibilidade(Visibilidade visibilidade) {
        this.visibilidade = visibilidade;
    }

    public LocalDate getDataLavratura() {
        return dataLavratura;
    }

    public void setDataLavratura(LocalDate dataLavratura) {
        this.dataLavratura = dataLavratura;
    }

    public String getLavradoPor() {
        return lavradoPor;
    }

    public void setLavradoPor(String lavradoPor) {
        this.lavradoPor = lavradoPor;
    }

    public String getLivro() {
        return livro;
    }

    public void setLivro(String livro) {
        this.livro = livro;
    }

    public String getFolha() {
        return folha;
    }

    public void setFolha(String folha) {
        this.folha = folha;
    }

    public String getTermo() {
        return termo;
    }

    public void setTermo(String termo) {
        this.termo = termo;
    }

    public String getParoquiaCustodiante() {
        return paroquiaCustodiante;
    }

    public void setParoquiaCustodiante(String paroquiaCustodiante) {
        this.paroquiaCustodiante = paroquiaCustodiante;
    }

    public LocalDateTime getDataCelebracao() {
        return dataCelebracao;
    }

    public void setDataCelebracao(LocalDateTime dataCelebracao) {
        this.dataCelebracao = dataCelebracao;
    }

    public String getLocalCelebracao() {
        return localCelebracao;
    }

    public void setLocalCelebracao(String localCelebracao) {
        this.localCelebracao = localCelebracao;
    }

    public boolean isDadoSensivel() {
        return dadoSensivel;
    }

    public void setDadoSensivel(boolean dadoSensivel) {
        this.dadoSensivel = dadoSensivel;
    }

    public Map<String, String> getDadosEspecificos() {
        return dadosEspecificos;
    }

    public void setDadosEspecificos(Map<String, String> dadosEspecificos) {
        this.dadosEspecificos = new HashMap<>(dadosEspecificos);
    }

    public String getHomologadoPor() {
        return homologadoPor;
    }

    public void setHomologadoPor(String homologadoPor) {
        this.homologadoPor = homologadoPor;
    }

    public Instant getHomologadoEmUtc() {
        return homologadoEmUtc;
    }

    public void setHomologadoEmUtc(Instant homologadoEmUtc) {
        this.homologadoEmUtc = homologadoEmUtc;
    }

    public String getMotivoCancelamento() {
        return motivoCancelamento;
    }

    public void setMotivoCancelamento(String motivoCancelamento) {
        this.motivoCancelamento = motivoCancelamento;
    }

    public String getCanceladoPor() {
        return canceladoPor;
    }

    public void setCanceladoPor(String canceladoPor) {
        this.canceladoPor = canceladoPor;
    }

    public Instant getCanceladoEmUtc() {
        return canceladoEmUtc;
    }

    public void setCanceladoEmUtc(Instant canceladoEmUtc) {
        this.canceladoEmUtc = canceladoEmUtc;
    }

    public List<AssentoParticipanteEntity> getParticipantes() {
        return participantes;
    }

    public List<AverbacaoBatismoEntity> getAverbacoes() {
        return averbacoes;
    }

    public List<AssentoRessalvaEntity> getRessalvas() {
        return ressalvas;
    }

    public List<AssentoNotificacaoEntity> getNotificacoes() {
        return notificacoes;
    }
}
