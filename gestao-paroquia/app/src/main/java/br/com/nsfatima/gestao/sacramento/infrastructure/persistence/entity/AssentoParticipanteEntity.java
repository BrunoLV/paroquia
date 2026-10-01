package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.sacramento.domain.model.PapelParticipante;
import br.com.nsfatima.gestao.support.infrastructure.crypto.EncryptedStringConverter;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
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

import java.util.UUID;

/** Entidade JPA de {@code calendario.assento_participantes}: snapshot textual e vínculo tardio opcional. */
@Entity
@Table(name = "assento_participantes", schema = "calendario")
public class AssentoParticipanteEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assento_id", nullable = false)
    private AssentoSacramentalEntity assento;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false, length = 32)
    private PapelParticipante papel;

    @Column(name = "nome_historico", nullable = false)
    private String nomeHistorico;

    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "documento", length = 2048)
    private String documento;

    @Column(name = "local_nascimento")
    private String localNascimento;

    @Column(name = "catolico")
    private Boolean catolico;

    @Column(name = "crismado")
    private Boolean crismado;

    @Column(name = "idade_na_celebracao")
    private Integer idadeNaCelebracao;

    @Column(name = "condicao_canonica", length = 16)
    private String condicaoCanonica;

    @Column(name = "fiel_id")
    private UUID fielId;

    @Column(name = "clerigo_id")
    private UUID clerigoId;

    @Column(name = "vinculo_pendente", nullable = false)
    private boolean vinculoPendente;

    public AssentoParticipanteEntity() {
    }

    public AssentoParticipanteEntity(UUID id, AssentoSacramentalEntity assento) {
        this.id = id;
        this.assento = assento;
    }

    public UUID getId() {
        return id;
    }

    public PapelParticipante getPapel() {
        return papel;
    }

    public void setPapel(PapelParticipante papel) {
        this.papel = papel;
    }

    public String getNomeHistorico() {
        return nomeHistorico;
    }

    public void setNomeHistorico(String nomeHistorico) {
        this.nomeHistorico = nomeHistorico;
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getLocalNascimento() {
        return localNascimento;
    }

    public void setLocalNascimento(String localNascimento) {
        this.localNascimento = localNascimento;
    }

    public Boolean getCatolico() {
        return catolico;
    }

    public void setCatolico(Boolean catolico) {
        this.catolico = catolico;
    }

    public Boolean getCrismado() {
        return crismado;
    }

    public void setCrismado(Boolean crismado) {
        this.crismado = crismado;
    }

    public Integer getIdadeNaCelebracao() {
        return idadeNaCelebracao;
    }

    public void setIdadeNaCelebracao(Integer idadeNaCelebracao) {
        this.idadeNaCelebracao = idadeNaCelebracao;
    }

    public String getCondicaoCanonica() {
        return condicaoCanonica;
    }

    public void setCondicaoCanonica(String condicaoCanonica) {
        this.condicaoCanonica = condicaoCanonica;
    }

    public UUID getFielId() {
        return fielId;
    }

    public void setFielId(UUID fielId) {
        this.fielId = fielId;
    }

    public UUID getClerigoId() {
        return clerigoId;
    }

    public void setClerigoId(UUID clerigoId) {
        this.clerigoId = clerigoId;
    }

    public boolean isVinculoPendente() {
        return vinculoPendente;
    }

    public void setVinculoPendente(boolean vinculoPendente) {
        this.vinculoPendente = vinculoPendente;
    }
}
