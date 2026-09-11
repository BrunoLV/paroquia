package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Regra de recorrência completa de uma série de eventos, com todos os campos necessários para
 * reconstruir o formulário de edição/visualização no cliente (frequência, dias, limites) — ao
 * contrário de {@link EventoRecorrenciaResponse}, que só identifica a série.
 */
public record EventoRecorrenciaDetalheResponse(
        UUID id,
        UUID eventoBaseId,
        String frequencia,
        int intervalo,
        List<DayOfWeek> diasDaSemana,
        String posicaoNoMes,
        String posicaoNoAno,
        Integer mesDoAno,
        Integer diaDoMes,
        LocalDate dataLimite,
        Integer numeroOcorrencias) {

    public static EventoRecorrenciaDetalheResponse de(EventoRecorrenciaEntity entity) {
        RegraRecorrencia regra = entity.getRegra();
        return new EventoRecorrenciaDetalheResponse(
                entity.getId(),
                entity.getEventoBaseId(),
                regra.frequencia(),
                regra.intervalo(),
                regra.diasDaSemana(),
                regra.posicaoNoMes(),
                regra.posicaoNoAno(),
                regra.mesDoAno(),
                regra.diaDoMes(),
                regra.dataLimite(),
                regra.numeroOcorrencias());
    }
}
