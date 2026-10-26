package br.com.nsfatima.gestao.reserva.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.infrastructure.persistence.entity.ReservaEntity;
import org.springframework.stereotype.Component;

/**
 * Tradutor entre a entidade JPA e o agregado Reserva. Na leitura, a margem logística de reservas legadas, gravadas
 * antes do teto de {@link IntervaloReserva#BUFFER_MAXIMO_MINUTOS}, é limitada a esse teto em vez de impedir o
 * carregamento da reserva.
 */
@Component
public class ReservaMapper {

    public Reserva toDomain(ReservaEntity entity) {
        if (entity == null) {
            return null;
        }

        IntervaloReserva intervalo = new IntervaloReserva(
                entity.getDataHoraInicio(),
                entity.getDataHoraFim(),
                Math.min(entity.getBufferMinutos(), IntervaloReserva.BUFFER_MAXIMO_MINUTOS)
        );

        return new Reserva(
                new ReservaId(entity.getId()),
                TipoItemReserva.fromString(entity.getTipoItem()),
                entity.getItemId(),
                intervalo,
                entity.getDescricao(),
                entity.getSolicitanteId(),
                entity.getPastoralMovimentoId(),
                entity.getEventoId(),
                entity.getGrupoRecorrenciaId(),
                StatusReserva.fromString(entity.getStatus()),
                entity.getAprovadorId(),
                entity.getAprovadoEm(),
                entity.getMotivoCancelamento(),
                entity.getCanceladoPorId(),
                entity.getCheckAvarias(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getVersion()
        );
    }

    public ReservaEntity toEntity(Reserva domain) {
        if (domain == null) {
            return null;
        }

        return new ReservaEntity(
                domain.getId().value(),
                domain.getTipoItem().name(),
                domain.getItemId(),
                domain.getIntervalo().dataHoraInicio(),
                domain.getIntervalo().dataHoraFim(),
                domain.getIntervalo().bufferMinutos(),
                domain.getDescricao(),
                domain.getSolicitanteId(),
                domain.getPastoralMovimentoId(),
                domain.getEventoId(),
                domain.getGrupoRecorrenciaId(),
                domain.getStatus().name(),
                domain.getAprovadorId(),
                domain.getAprovadoEm(),
                domain.getMotivoCancelamento(),
                domain.getCanceladoPorId(),
                domain.getCheckAvarias(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getDeletedAt(),
                domain.getVersion()
        );
    }
}
