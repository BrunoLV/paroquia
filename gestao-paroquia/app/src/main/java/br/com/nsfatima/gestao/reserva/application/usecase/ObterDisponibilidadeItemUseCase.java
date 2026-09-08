package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.dto.DisponibilidadeFaixaDto;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ObterDisponibilidadeItemUseCase {

    private final ReservaRepository reservaRepository;

    public ObterDisponibilidadeItemUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeFaixaDto> executar(TipoItemReserva tipoItem, UUID itemId, LocalDate data) {
        Objects.requireNonNull(tipoItem, "Tipo de item é obrigatório.");
        Objects.requireNonNull(itemId, "ID do item é obrigatório.");
        LocalDate dia = data != null ? data : LocalDate.now(ZoneId.of("America/Sao_Paulo"));

        Instant inicioDia = dia.atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant();
        Instant fimDia = dia.plusDays(1).atStartOfDay(ZoneId.of("America/Sao_Paulo")).toInstant();

        ReservaFiltro filtro = new ReservaFiltro(
                tipoItem,
                itemId,
                null,
                null,
                null,
                null,
                inicioDia,
                fimDia,
                null
        );

        List<Reserva> reservas = reservaRepository.findAll(filtro);

        return reservas.stream()
                .filter(r -> r.getStatus().ocupaAgenda())
                .map(r -> new DisponibilidadeFaixaDto(
                        r.getId().value(),
                        r.getIntervalo().dataHoraInicio(),
                        r.getIntervalo().dataHoraFim(),
                        r.getIntervalo().fimComBuffer(),
                        r.getIntervalo().bufferMinutos(),
                        r.getStatus(),
                        "Reservado",
                        r.getDescricao()
                ))
                .toList();
    }
}
