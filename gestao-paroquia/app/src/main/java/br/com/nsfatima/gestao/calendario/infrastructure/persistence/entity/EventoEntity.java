package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.calendario.domain.model.EstadoConflitoAgenda;
import br.com.nsfatima.gestao.calendario.domain.type.CategoriaEvento;
import br.com.nsfatima.gestao.calendario.domain.type.FormatoEvento;
import br.com.nsfatima.gestao.calendario.domain.type.TipoHorarioEvento;
import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

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
@SuppressWarnings("PMD.ExcessivePublicCount")
public class EventoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, length = 160)
    private String titulo;

    @Column(length = 4000)
    private String descricao;

    @Column(name = "grupo_paroquial_responsavel_id", nullable = false)
    private UUID grupoParoquialResponsavelId;

    @Column(name = "inicio_utc", nullable = false)
    private Instant inicioUtc;

    @Column(name = "fim_utc", nullable = false)
    private Instant fimUtc;

    /** Formato de horário do evento (Personalizado, Dia Inteiro, Madrugada, Manhã, Tarde ou Noite). */
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_horario", length = 32, nullable = false)
    private TipoHorarioEvento tipoHorario = TipoHorarioEvento.PERSONALIZADO;

    /** Lista de períodos/turnos selecionados quando o horário é por turnos (ex: MANHA,NOITE). */
    @Column(name = "periodos", length = 128)
    private String periodos;

    /** Formato de realização do evento (Presencial, Online, Híbrido). */
    @Enumerated(EnumType.STRING)
    @Column(name = "formato", length = 32, nullable = false)
    private FormatoEvento formato;

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

    /**
     * Múltiplos espaços físicos/locais alocados para o evento;
     * carregado antecipadamente (EAGER) e mapeado como somente leitura a partir da tabela
     * {@code eventos_locais} (associação gerenciada via {@link EventoLocalEntity}).
     */
    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "evento_id", insertable = false, updatable = false)
    private List<EventoLocalEntity> locais = new ArrayList<>();

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

    /**
     * Organizações envolvidas no evento. Retorna uma <em>view</em> imutável — a associação é
     * mapeada como somente leitura ({@code insertable=false, updatable=false}); a manutenção real
     * dos vínculos é feita via {@code EventoEnvolvidoEntity}. Para (re)carregar a representação
     * usada na resposta, use {@link #definirEnvolvidosCarregados(List)}.
     */
    public List<EventoEnvolvidoEntity> getEnvolvidos() {
        return envolvidos == null ? List.of() : Collections.unmodifiableList(envolvidos);
    }

    /** Substitui a coleção de envolvidos carregada para montagem da resposta (não persiste). */
    public void definirEnvolvidosCarregados(List<EventoEnvolvidoEntity> envolvidos) {
        this.envolvidos = envolvidos == null ? List.of() : new ArrayList<>(envolvidos);
    }

    /**
     * Múltiplos espaços físicos/locais vinculados ao evento.
     */
    public List<EventoLocalEntity> getLocais() {
        return locais == null ? List.of() : Collections.unmodifiableList(locais);
    }

    /**
     * Retorna a lista unificada de identificadores de local (UUID) alocados para o evento.
     * Se houver registros na tabela de junção {@code eventos_locais}, retorna seus IDs;
     * caso contrário, retorna o {@code localId} legado, se não nulo.
     */
    public List<UUID> getLocalIds() {
        if (locais != null && !locais.isEmpty()) {
            return locais.stream().map(EventoLocalEntity::getLocalId).toList();
        }
        return localId != null ? List.of(localId) : List.of();
    }

    /** Substitui a coleção de locais carregada para montagem da resposta (não persiste). */
    public void definirLocaisCarregados(List<EventoLocalEntity> locais) {
        this.locais = locais == null ? List.of() : new ArrayList<>(locais);
        if (this.localId == null && !this.locais.isEmpty()) {
            this.localId = this.locais.getFirst().getLocalId();
        }
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

    public TipoHorarioEvento getTipoHorario() {
        return tipoHorario;
    }

    public void setTipoHorario(TipoHorarioEvento tipoHorario) {
        this.tipoHorario = tipoHorario != null ? tipoHorario : TipoHorarioEvento.PERSONALIZADO;
    }

    public String getPeriodos() {
        return periodos;
    }

    public void setPeriodos(String periodos) {
        this.periodos = periodos;
    }

    public List<TipoHorarioEvento> getPeriodosList() {
        if (periodos == null || periodos.isBlank()) {
            return List.of();
        }
        return Arrays.stream(periodos.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return TipoHorarioEvento.valueOf(s);
                    } catch (IllegalArgumentException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public void setPeriodosList(List<TipoHorarioEvento> periodosList) {
        if (periodosList == null || periodosList.isEmpty()) {
            this.periodos = null;
        } else {
            this.periodos = periodosList.stream()
                    .filter(Objects::nonNull)
                    .map(TipoHorarioEvento::name)
                    .distinct()
                    .collect(Collectors.joining(","));
        }
    }

    public FormatoEvento getFormato() {
        if (formato == null) {
            return (localId != null || (locais != null && !locais.isEmpty()))
                    ? FormatoEvento.PRESENCIAL
                    : FormatoEvento.ONLINE;
        }
        return formato;
    }

    public void setFormato(FormatoEvento formato) {
        this.formato = formato;
    }

    @PrePersist
    @PreUpdate
    public void ensureFormato() {
        if (this.formato == null) {
            this.formato = getFormato();
        }
    }
}

