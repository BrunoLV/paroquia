package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoEncontradaException;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Encerra o uso de um local reservado (RN-RES-06): a reserva de local confirmada vai direto a {@code FINALIZADA},
 * sem conferência de avarias, depois que o uso começou. Diferente do ativo, o local não tem custódia a devolver,
 * portanto a finalização é um ato manual do solicitante, do coordenador ou da administração.
 */
@Service
public class FinalizarUsoLocalUseCase {

    private final ReservaRepository reservaRepository;
    private final ReservaAuthorizationService authorizationService;

    public FinalizarUsoLocalUseCase(
            ReservaRepository reservaRepository,
            ReservaAuthorizationService authorizationService
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.authorizationService = Objects.requireNonNull(authorizationService,
                "ReservaAuthorizationService é obrigatório.");
    }

    @Transactional
    public Reserva executar(UUID reservaId, UsuarioActor operador) {
        Objects.requireNonNull(reservaId, "ID da reserva é obrigatório.");
        Objects.requireNonNull(operador, "Operador é obrigatório.");

        Reserva reserva = reservaRepository.findById(new ReservaId(reservaId))
                .orElseThrow(() -> new ReservaNaoEncontradaException(reservaId));

        authorizationService.validarAutorizacaoFinalizacao(operador, reserva);
        reserva.finalizarUsoLocal(Instant.now());
        return reservaRepository.save(reserva);
    }
}
