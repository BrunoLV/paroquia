package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.dto.ReservaDetalheDto;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class ListarReservasUseCase {

    private final ReservaRepository reservaRepository;
    private final ReservaEnricherService enricherService;

    public ListarReservasUseCase(ReservaRepository reservaRepository, ReservaEnricherService enricherService) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.enricherService = Objects.requireNonNull(enricherService, "ReservaEnricherService é obrigatório.");
    }

    @Transactional(readOnly = true)
    public List<ReservaDetalheDto> executar(ReservaFiltro filtro) {
        List<Reserva> reservas = reservaRepository.findAll(filtro);
        return enricherService.enriquecer(reservas);
    }
}
