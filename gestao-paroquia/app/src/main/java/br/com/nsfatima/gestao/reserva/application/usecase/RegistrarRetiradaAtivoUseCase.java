package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoEncontradaException;
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
    private final PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    public RegistrarRetiradaAtivoUseCase(ReservaRepository reservaRepository,
                                        PublicadorReservaDeAtivo publicadorReservaDeAtivo) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.publicadorReservaDeAtivo = Objects.requireNonNull(publicadorReservaDeAtivo,
                "PublicadorReservaDeAtivo é obrigatório.");
    }

    @Transactional
    public Reserva executar(UUID reservaId, UsuarioActor operador) {
        Objects.requireNonNull(reservaId, "ID da reserva é obrigatório.");
        Objects.requireNonNull(operador, "Operador é obrigatório.");

        Reserva reserva = reservaRepository.findById(new ReservaId(reservaId))
                .orElseThrow(() -> new ReservaNaoEncontradaException(reservaId));

        reserva.registrarRetirada(operador.id(), Instant.now());
        Reserva salva = reservaRepository.save(reserva);
        publicadorReservaDeAtivo.publicarSeForAtivo(salva);
        return salva;
    }
}
