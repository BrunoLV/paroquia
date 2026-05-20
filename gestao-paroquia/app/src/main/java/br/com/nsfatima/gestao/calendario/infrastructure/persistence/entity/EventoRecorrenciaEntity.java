package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.converter.RegraRecorrenciaConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "eventos_recorrencia", schema = "calendario")
public class EventoRecorrenciaEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    @Column(name = "evento_base_id", nullable = false)
    private UUID eventoBaseId;

    @Column(nullable = false, length = 32)
    private String frequencia;

    @Column(nullable = false)
    private Integer intervalo;

    @Column(name = "regra_json", nullable = false, length = 4000)
    @Convert(converter = RegraRecorrenciaConverter.class)
    private RegraRecorrencia regra;

    @Column(name = "data_fim_utc")
    private Instant dataFimUtc;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEventoBaseId() {
        return eventoBaseId;
    }

    public void setEventoBaseId(UUID eventoBaseId) {
        this.eventoBaseId = eventoBaseId;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(String frequencia) {
        this.frequencia = frequencia;
    }

    public Integer getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(Integer intervalo) {
        this.intervalo = intervalo;
    }

    public RegraRecorrencia getRegra() {
        return regra;
    }

    public void setRegra(RegraRecorrencia regra) {
        this.regra = regra;
        if (regra != null) {
            this.frequencia = regra.frequencia();
            this.intervalo = regra.intervalo();
        }
    }

    public Instant getDataFimUtc() {
        return dataFimUtc;
    }

    public void setDataFimUtc(Instant dataFimUtc) {
        this.dataFimUtc = dataFimUtc;
    }
}
