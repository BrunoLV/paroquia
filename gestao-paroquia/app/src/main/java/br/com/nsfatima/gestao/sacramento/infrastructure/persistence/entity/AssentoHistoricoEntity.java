package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Entidade JPA de {@code calendario.assento_historico}: trilha estrita append-only (trigger veda UPDATE/DELETE). */
@Entity
@Table(name = "assento_historico", schema = "calendario")
public class AssentoHistoricoEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "assento_id", nullable = false)
    private UUID assentoId;

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

    @Column(name = "valores_anteriores", columnDefinition = "TEXT")
    private String valoresAnteriores;

    @Column(name = "valores_posteriores", columnDefinition = "TEXT")
    private String valoresPosteriores;

    @Column(name = "ocorrido_em_utc", nullable = false)
    private Instant ocorridoEmUtc;

    public AssentoHistoricoEntity() {
    }

    public AssentoHistoricoEntity(UUID id, UUID assentoId, String acao, UUID usuarioId, String ator, String perfil,
                                  String enderecoIp, String valoresAnteriores, String valoresPosteriores,
                                  Instant ocorridoEmUtc) {
        this.id = id;
        this.assentoId = assentoId;
        this.acao = acao;
        this.usuarioId = usuarioId;
        this.ator = ator;
        this.perfil = perfil;
        this.enderecoIp = enderecoIp;
        this.valoresAnteriores = valoresAnteriores;
        this.valoresPosteriores = valoresPosteriores;
        this.ocorridoEmUtc = ocorridoEmUtc;
    }

    public UUID getId() {
        return id;
    }

    public UUID getAssentoId() {
        return assentoId;
    }

    public String getAcao() {
        return acao;
    }

    public UUID getUsuarioId() {
        return usuarioId;
    }

    public String getAtor() {
        return ator;
    }

    public String getPerfil() {
        return perfil;
    }

    public String getEnderecoIp() {
        return enderecoIp;
    }

    public String getValoresAnteriores() {
        return valoresAnteriores;
    }

    public String getValoresPosteriores() {
        return valoresPosteriores;
    }

    public Instant getOcorridoEmUtc() {
        return ocorridoEmUtc;
    }
}
