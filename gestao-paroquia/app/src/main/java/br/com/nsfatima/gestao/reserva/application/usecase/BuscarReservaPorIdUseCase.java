package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.dto.ReservaDetalheDto;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoEncontradaException;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
public class BuscarReservaPorIdUseCase {

    private final ReservaRepository reservaRepository;
    private final ReservaEnricherService enricherService;

    public BuscarReservaPorIdUseCase(ReservaRepository reservaRepository, ReservaEnricherService enricherService) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.enricherService = Objects.requireNonNull(enricherService, "ReservaEnricherService é obrigatório.");
    }

    @Transactional(readOnly = true)
    public ReservaDetalheDto executar(UUID id) {
        Objects.requireNonNull(id, "ID da reserva é obrigatório.");
        Reserva reserva = reservaRepository.findById(new ReservaId(id))
                .orElseThrow(() -> new ReservaNaoEncontradaException(id));
        return enricherService.enriquecer(reserva);
    }
}
