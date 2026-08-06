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

/**
 * Entidade central do contexto de calendário: representa um evento/celebração agendado na
 * paróquia (missa, celebração litúrgica, reunião pastoral, atividade de movimento, etc.).
 * É o maior e mais conectado agregado do sistema ("god node" no grafo de dependências),
 * concentrando dados de agendamento, categorização litúrgica/pastoral, detecção de conflito
 * de agenda, vínculo com recorrência e integração com o fluxo de aprovação. Mutações não
 * triviais (criar/atualizar/cancelar) tipicamente passam antes pelo fluxo de aprovação
 * (ver {@code AprovacaoEntity}) em vez de alterar esta entidade diretamente.
 */
@Entity
@Table(name = "eventos", schema = "calendario")
public class EventoEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(length = 4000)
    private String descricao;

    @Column(name = "grupoParoquial_responsavel_id", nullable = false)
    private UUID grupoParoquialResponsavelId;

    @Column(name = "inicio_utc", nullable = false)
    private Instant inicioUtc;

    @Column(name = "fim_utc", nullable = false)
    private Instant fimUtc;

    /**
     * Status do ciclo de vida do evento (ex.: RASCUNHO, CONFIRMADO, ADICIONADO_EXTRA,
     * CANCELADO). Mantido como {@code String} em vez de um único enum porque o valor
     * persistido precisa acomodar vocabulários distintos entre entrada
     * ({@code EventoStatusInput}) e saída ({@code EventoStatusResponse}, que inclui o
     * marcador de compatibilidade {@code UNKNOWN_LEGACY} para dados legados inconsistentes).
     */
    @Column(nullable = false, length = 32)
    private String status;

    /** Motivo/justificativa informado ao cancelar o evento; preenchido somente quando o status é de cancelamento. */
    @Column(name = "cancelado_motivo", length = 2000)
    private String canceladoMotivo;

    /**
     * Justificativa obrigatória quando o evento é adicionado fora do fluxo normal de
     * planejamento (status ADICIONADO_EXTRA), documentando por que a exceção foi aceita.
     */
    @Column(name = "adicionado_extra_justificativa", length = 4000)
    private String adicionadoExtraJustificativa;

    /**
     * Estado de conflito de agenda detectado para este evento (ex.: sobreposição de horário
     * ou local com outro evento). {@code null} indica ausência de conflito conhecido; o único
     * valor hoje modelado é {@code PENDENTE}, sinalizando conflito ainda não resolvido.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "conflict_state", length = 32)
    private EstadoConflitoAgenda estadoConflito;

    /** Descrição do motivo do conflito de agenda quando {@link #estadoConflito} está preenchido. */
    @Column(name = "conflict_reason", length = 2000)
    private String motivoConflito;

    /**
     * Referência à série de recorrência ({@code EventoRecorrenciaEntity}) que originou este
     * evento, quando ele é uma ocorrência de um evento recorrente; {@code null} para eventos
     * avulsos (não recorrentes).
     */
    @Column(name = "recorrencia_id")
    private UUID recorrenciaId;

    /** Identificador do projeto ao qual este evento está vinculado, quando aplicável. */
    @Column(name = "projeto_id")
    private UUID projetoId;

    /**
     * Categoria do evento (litúrgico, sacramental, pastoral, formativo, administrativo etc.),
     * que determina o nível de prioridade padrão usado na resolução de conflitos de agenda
     * (ver {@link CategoriaEvento#getNivelPrioridade()}).
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 32)
    private CategoriaEvento categoria;

    /** Ano litúrgico de referência ({@code AnoLiturgicoEntity}) usado para contextualizar o evento. */
    @Column(name = "ano_liturgico_ref")
    private Integer anoLiturgicoRef;

    /** Ciclo/tempo litúrgico ({@code CicloLiturgicoEntity}) vigente no momento do evento, quando aplicável. */
    @Column(name = "ciclo_liturgico_id")
    private UUID cicloLiturgicoId;

    /** Identificador do local físico onde o evento ocorre. */
    @Column(name = "local_id")
    private UUID localId;

    /**
     * Identificador da igreja/comunidade (Matriz ou Comunidade) de contexto do evento,
     * usado para escopar autorização quando a organização responsável é uma pastoral ou
     * movimento sem hierarquia territorial própria.
     */
    @Column(name = "igreja_vinculo_id")
    private UUID igrejaVinculoId;

    /**
     * Nível de prioridade efetivo do evento na resolução de conflitos de agenda (quanto
     * menor o valor, maior a prioridade); o padrão 4 corresponde à prioridade mais baixa,
     * sendo normalmente sobrescrito a partir da {@link #categoria} do evento.
     */
    @Column(name = "nivel_prioridade", nullable = false)
    private Integer nivelPrioridade = 4;

    /**
     * Indica se o evento foi explicitamente autorizado pelo Conselho Paroquial de Pastoral
     * (CPP), permitindo, por exemplo, flexibilizar regras de agendamento que dependem dessa
     * autorização.
     */
    @Column(name = "autorizado_pelo_cpp", nullable = false)
    private Boolean autorizadoPeloCpp = false;

    /** Setor pastoral responsável pelo evento, quando a organização vinculada pertence a um setor específico. */
    @Enumerated(EnumType.STRING)
    @Column(name = "setor_pastoral", length = 64)
    private SetorPastoral setorPastoral;


    /**
     * Organizações envolvidas no evento e seus papéis de participação (responsável/apoio);
     * carregado antecipadamente (EAGER) e mapeado como somente leitura a partir da tabela
     * {@code eventos_envolvidos} (associação gerenciada via {@link EventoEnvolvidoEntity}).
     */
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

