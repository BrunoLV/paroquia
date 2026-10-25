package br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.conselho.domain.model.CaraterReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ModalidadeReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
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
 * Entidade JPA da ata de reunião. O conteúdo estruturado (pauta, deliberações, presença) fica serializado em
 * {@code conteudo}; as colunas escalares de título, data, caráter e modalidade são projeções desse conteúdo,
 * mantidas para filtro e listagem sem desserializar. Atas seladas são imutáveis também no banco (trigger).
 */
@Entity
@Table(name = "ata_reuniao", schema = "calendario")
public class AtaReuniaoEntity extends BaseAuditableEntity {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 32)
    private TipoReuniao tipo;

    @Column(name = "organizacao_convocante_id", nullable = false)
    private UUID organizacaoConvocanteId;

    @Column(name = "organizacao_associada_id")
    private UUID organizacaoAssociadaId;

    @Column(name = "titulo", nullable = false, length = 160)
    private String titulo;

    @Column(name = "data_reuniao", nullable = false)
    private LocalDate dataReuniao;

    @Enumerated(EnumType.STRING)
    @Column(name = "carater", nullable = false, length = 16)
    private CaraterReuniao carater;

    @Enumerated(EnumType.STRING)
    @Column(name = "modalidade", nullable = false, length = 16)
    private ModalidadeReuniao modalidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 24)
    private StatusAta status;

    @Column(name = "conteudo", nullable = false, columnDefinition = "text")
    private String conteudo;

    @Column(name = "numero")
    private Integer numero;

    @Column(name = "ano")
    private Integer ano;

    @Column(name = "membros_convocados")
    private Integer membrosConvocados;

    @Column(name = "redator_id", nullable = false, length = 128)
    private String redatorId;

    @Column(name = "homologador_id", length = 128)
    private String homologadorId;

    @Column(name = "motivo_veto", length = 2000)
    private String motivoVeto;

    @Column(name = "decidido_em_utc")
    private Instant decididoEmUtc;

    @Column(name = "ata_retificada_id")
    private UUID ataRetificadaId;

    @Column(name = "justificativa_retificacao", length = 2000)
    private String justificativaRetificacao;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public TipoReuniao getTipo() {
        return tipo;
    }

    public void setTipo(TipoReuniao tipo) {
        this.tipo = tipo;
    }

    public UUID getOrganizacaoConvocanteId() {
        return organizacaoConvocanteId;
    }

    public void setOrganizacaoConvocanteId(UUID organizacaoConvocanteId) {
        this.organizacaoConvocanteId = organizacaoConvocanteId;
    }

    public UUID getOrganizacaoAssociadaId() {
        return organizacaoAssociadaId;
    }

    public void setOrganizacaoAssociadaId(UUID organizacaoAssociadaId) {
        this.organizacaoAssociadaId = organizacaoAssociadaId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public LocalDate getDataReuniao() {
        return dataReuniao;
    }

    public void setDataReuniao(LocalDate dataReuniao) {
        this.dataReuniao = dataReuniao;
    }

    public CaraterReuniao getCarater() {
        return carater;
    }

    public void setCarater(CaraterReuniao carater) {
        this.carater = carater;
    }

    public ModalidadeReuniao getModalidade() {
        return modalidade;
    }

    public void setModalidade(ModalidadeReuniao modalidade) {
        this.modalidade = modalidade;
    }

    public StatusAta getStatus() {
        return status;
    }

    public void setStatus(StatusAta status) {
        this.status = status;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public Integer getAno() {
        return ano;
    }

    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public Integer getMembrosConvocados() {
        return membrosConvocados;
    }

    public void setMembrosConvocados(Integer membrosConvocados) {
        this.membrosConvocados = membrosConvocados;
    }

    public String getRedatorId() {
        return redatorId;
    }

    public void setRedatorId(String redatorId) {
        this.redatorId = redatorId;
    }

    public String getHomologadorId() {
        return homologadorId;
    }

    public void setHomologadorId(String homologadorId) {
        this.homologadorId = homologadorId;
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

    public UUID getAtaRetificadaId() {
        return ataRetificadaId;
    }

    public void setAtaRetificadaId(UUID ataRetificadaId) {
        this.ataRetificadaId = ataRetificadaId;
    }

    public String getJustificativaRetificacao() {
        return justificativaRetificacao;
    }

    public void setJustificativaRetificacao(String justificativaRetificacao) {
        this.justificativaRetificacao = justificativaRetificacao;
    }
}
