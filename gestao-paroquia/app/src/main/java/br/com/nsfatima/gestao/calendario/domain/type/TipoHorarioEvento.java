package br.com.nsfatima.gestao.calendario.domain.type;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Define as opções semânticas de formato e faixa horária para agendamento de eventos.
 * Permite alocação em turnos fixos (Madrugada, Manhã, Tarde, Noite), Dia Inteiro (24h)
 * ou horário sob medida (Personalizado).
 */
public enum TipoHorarioEvento {

    PERSONALIZADO("Horário Personalizado", false, null, null),
    DIA_INTEIRO("Dia Inteiro", true, LocalTime.of(0, 0, 0), LocalTime.of(23, 59, 59)),
    MADRUGADA("Madrugada", true, LocalTime.of(0, 0, 0), LocalTime.of(5, 59, 59)),
    MANHA("Manhã", true, LocalTime.of(6, 0, 0), LocalTime.of(11, 59, 59)),
    TARDE("Tarde", true, LocalTime.of(12, 0, 0), LocalTime.of(17, 59, 59)),
    NOITE("Noite", true, LocalTime.of(18, 0, 0), LocalTime.of(23, 59, 59));

    private final String descricao;
    private final boolean predefinido;
    private final LocalTime horaInicio;
    private final LocalTime horaFim;

    TipoHorarioEvento(String descricao, boolean predefinido, LocalTime horaInicio, LocalTime horaFim) {
        this.descricao = descricao;
        this.predefinido = predefinido;
        this.horaInicio = horaInicio;
        this.horaFim = horaFim;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isPredefinido() {
        return predefinido;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public LocalTime getHoraFim() {
        return horaFim;
    }

    /**
     * Margem de logística em minutos. Para períodos e dia inteiro, o tempo de troca de turno
     * já está embutido na divisão dos blocos, dispensando buffer externo (buffer = 0).
     */
    public int getBufferMinutos() {
        return this.predefinido ? 0 : 30;
    }

    /**
     * Calcula o intervalo exato de início e fim no fuso horário indicado para a data fornecida.
     */
    public IntervaloCalculado calcularIntervalo(LocalDate data, ZoneId zoneId) {
        if (!this.predefinido) {
            throw new IllegalStateException("Cálculo automático de faixa só se aplica a formatos pré-definidos");
        }
        Instant inicio = data.atTime(this.horaInicio).atZone(zoneId).toInstant();
        Instant fim = data.atTime(this.horaFim).atZone(zoneId).toInstant();
        return new IntervaloCalculado(inicio, fim);
    }

    public record IntervaloCalculado(Instant inicioUtc, Instant fimUtc) {
    }
}
