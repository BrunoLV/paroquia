package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseAuditableEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.util.UUID;

/**
 * Entidade relacional JPA da tabela {@code calendario.ativo_manutencoes}. Cada linha é um período
 * de manutenção de um ativo; {@code dataFimReal} nula indica manutenção em aberto.
 */
@Entity
@Table(name = "ativo_manutencoes", schema = "calendario")
public class AtivoManutencaoEntity extends BaseAuditableEntity {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "ativo_id", nullable = false)
    private UUID ativoId;

    @Column(name = "descricao_reparo", nullable = false, columnDefinition = "TEXT")
    private String descricaoReparo;

    @Column(name = "data_inicio", nullable = false)
    private Instant dataInicio;

    @Column(name = "data_fim_prevista", nullable = false)
    private Instant dataFimPrevista;

    @Column(name = "data_fim_real")
    private Instant dataFimReal;

    @Column(name = "registrado_por_id", nullable = false)
    private UUID registradoPorId;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public AtivoManutencaoEntity() {
    }

    public AtivoManutencaoEntity(
            UUID id,
            UUID ativoId,
            String descricaoReparo,
            Instant dataInicio,
            Instant dataFimPrevista,
            Instant dataFimReal,
            UUID registradoPorId,
            Instant createdAt,
            Instant updatedAt,
            long version
    ) {
        this.id = id;
        this.ativoId = ativoId;
        this.descricaoReparo = descricaoReparo;
        this.dataInicio = dataInicio;
        this.dataFimPrevista = dataFimPrevista;
        this.dataFimReal = dataFimReal;
        this.registradoPorId = registradoPorId;
        setCriadoEmUtc(createdAt);
        setAtualizadoEmUtc(updatedAt);
        this.version = version;
    }

    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getAtivoId() {
        return ativoId;
    }
    public void setAtivoId(UUID ativoId) {
        this.ativoId = ativoId;
    }

    public String getDescricaoReparo() {
        return descricaoReparo;
    }
    public void setDescricaoReparo(String descricaoReparo) {
        this.descricaoReparo = descricaoReparo;
    }

    public Instant getDataInicio() {
        return dataInicio;
    }
    public void setDataInicio(Instant dataInicio) {
        this.dataInicio = dataInicio;
    }

    public Instant getDataFimPrevista() {
        return dataFimPrevista;
    }
    public void setDataFimPrevista(Instant dataFimPrevista) {
        this.dataFimPrevista = dataFimPrevista;
    }

    public Instant getDataFimReal() {
        return dataFimReal;
    }
    public void setDataFimReal(Instant dataFimReal) {
        this.dataFimReal = dataFimReal;
    }

    public UUID getRegistradoPorId() {
        return registradoPorId;
    }
    public void setRegistradoPorId(UUID registradoPorId) {
        this.registradoPorId = registradoPorId;
    }

    @Override
    public Long getVersion() {
        return version;
    }
    public void setVersion(long version) {
        this.version = version;
    }
}
