package br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Entidade JPA de {@code calendario.ata_historico}: trilha append-only (o banco veda UPDATE e DELETE). */
@Entity
@Table(name = "ata_historico", schema = "calendario")
public class AtaHistoricoEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "ata_id", nullable = false)
    private UUID ataId;

    @Column(name = "acao", nullable = false, length = 32)
    private String acao;

    @Column(name = "usuario_id")
    private UUID usuarioId;

    @Column(name = "ator", nullable = false, length = 128)
    private String ator;

    @Column(name = "perfil", length = 64)
    private String perfil;

    @Column(name = "endereco_ip", length = 64)
    private String enderecoIp;

    @Column(name = "valores_anteriores", columnDefinition = "text")
    private String valoresAnteriores;

    @Column(name = "valores_posteriores", columnDefinition = "text")
    private String valoresPosteriores;

    @Column(name = "ocorrido_em_utc", nullable = false)
    private Instant ocorridoEmUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAtaId() {
        return ataId;
    }

    public void setAtaId(UUID ataId) {
        this.ataId = ataId;
    }

    public String getAcao() {
        return acao;
    }

    public void setAcao(String acao) {
        this.acao = acao;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(UUID usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getAtor() {
        return ator;
    }

    public void setAtor(String ator) {
        this.ator = ator;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public String getEnderecoIp() {
        return enderecoIp;
    }

    public void setEnderecoIp(String enderecoIp) {
        this.enderecoIp = enderecoIp;
    }

    public String getValoresAnteriores() {
        return valoresAnteriores;
    }

    public void setValoresAnteriores(String valoresAnteriores) {
        this.valoresAnteriores = valoresAnteriores;
    }

    public String getValoresPosteriores() {
        return valoresPosteriores;
    }

    public void setValoresPosteriores(String valoresPosteriores) {
        this.valoresPosteriores = valoresPosteriores;
    }

    public Instant getOcorridoEmUtc() {
        return ocorridoEmUtc;
    }

    public void setOcorridoEmUtc(Instant ocorridoEmUtc) {
        this.ocorridoEmUtc = ocorridoEmUtc;
    }
}
