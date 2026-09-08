package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNotFoundException;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class RegistrarRetiradaAtivoUseCase {

    private final ReservaRepository reservaRepository;

    public RegistrarRetiradaAtivoUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
    }

    @Transactional
    public Reserva executar(UUID reservaId, UsuarioActor operador) {
        Objects.requireNonNull(reservaId, "ID da reserva é obrigatório.");
        Objects.requireNonNull(operador, "Operador é obrigatório.");

        // Secretaria, sacristia, admin ou coordenador da pastoral dona
        if (!operador.isAdminOuParoco()) {
            // Se for coordenador pode registrar a retirada do bem sob sua custódia
        }

        Reserva reserva = reservaRepository.findById(new ReservaId(reservaId))
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));

        reserva.registrarRetirada(operador.id(), Instant.now());
        return reservaRepository.save(reserva);
    }
}
