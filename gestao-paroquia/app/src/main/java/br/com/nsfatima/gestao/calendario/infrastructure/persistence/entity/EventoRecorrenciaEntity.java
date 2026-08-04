package br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.converter.RegraRecorrenciaConverter;
import br.com.nsfatima.gestao.support.infrastructure.persistence.entity.BaseVersionedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * Define a série de recorrência de um evento recorrente: a partir de um evento base
 * ({@link #eventoBaseId}), armazena a regra completa ({@link RegraRecorrencia}) usada para
 * gerar as datas das ocorrências futuras, além de projeções desnormalizadas de frequência
 * e intervalo (mantidas em sincronia com a regra via {@link #setRegra}) para facilitar
 * consultas simples sem precisar desserializar o JSON completo. Cada ocorrência concreta
 * gerada a partir desta série é persistida como um {@link EventoEntity} cujo
 * {@code recorrenciaId} aponta de volta para esta entidade.
 */
@Entity
@Table(name = "eventos_recorrencia", schema = "calendario")
public class EventoRecorrenciaEntity extends BaseVersionedEntity {

    @Id
    private UUID id;

    /** Identificador do evento original a partir do qual as ocorrências recorrentes são geradas. */
    @Column(name = "evento_base_id", nullable = false)
    private UUID eventoBaseId;

    /**
     * Frequência da recorrência (ex.: DIARIA, SEMANAL, MENSAL, ANUAL), espelhando
     * {@link RegraRecorrencia#frequencia()}; mantida desnormalizada para consultas
     * sem precisar desserializar {@link #regra}.
     */
    @Column(nullable = false, length = 32)
    private String frequencia;

    /** Intervalo de repetição (ex.: a cada N dias/semanas/meses/anos), espelhando {@link RegraRecorrencia#intervalo()}.
     * */
    @Column(nullable = false)
    private Integer intervalo;

    /**
     * Regra completa de recorrência (frequência, intervalo, dias da semana, limites etc.),
     * persistida como JSON via {@link RegraRecorrenciaConverter} na coluna {@code regra_json}.
     */
    @Column(name = "regra_json", nullable = false, length = 4000)
    @Convert(converter = RegraRecorrenciaConverter.class)
    private RegraRecorrencia regra;

    /** Data/hora (UTC) limite para geração de novas ocorrências desta série; {@code null} significa sem data-limite
     * explícita. */
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

    /**
     * Define a regra de recorrência e sincroniza os campos desnormalizados
     * {@link #frequencia} e {@link #intervalo} a partir dela, garantindo que ambas as
     * representações (JSON completo e colunas simples) permaneçam consistentes.
     *
     * @param regra nova regra de recorrência; se {@code null}, os campos desnormalizados não são alterados
     */
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
