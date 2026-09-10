package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNotFoundException;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaConflitoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class AprovarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final ReservaAuthorizationService authorizationService;
    private final ReservaConflitoService conflitoService;
    private final PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    public AprovarReservaUseCase(
            ReservaRepository reservaRepository,
            ReservaAuthorizationService authorizationService,
            ReservaConflitoService conflitoService,
            PublicadorReservaDeAtivo publicadorReservaDeAtivo
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.authorizationService = Objects.requireNonNull(authorizationService,
                "ReservaAuthorizationService é obrigatório.");
        this.conflitoService = Objects.requireNonNull(conflitoService, "ReservaConflitoService é obrigatório.");
        this.publicadorReservaDeAtivo = Objects.requireNonNull(publicadorReservaDeAtivo,
                "PublicadorReservaDeAtivo é obrigatório.");
    }

    @Transactional
    public Reserva executar(UUID reservaId, UsuarioActor operador) {
        Objects.requireNonNull(reservaId, "ID da reserva é obrigatório.");
        Objects.requireNonNull(operador, "Operador é obrigatório.");

        Reserva reserva = reservaRepository.findById(new ReservaId(reservaId))
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));

        authorizationService.validarAutorizacaoAprovacao(operador, reserva);

        // Revalida colisão com bloqueio pessimista antes de aprovar
        conflitoService.validarDisponibilidadeComLock(
                reserva.getTipoItem(),
                reserva.getItemId(),
                "Recurso",
                reserva.getIntervalo(),
                reserva.getId().value()
        );

        reserva.aprovar(operador.id(), Instant.now());
        Reserva salva = reservaRepository.save(reserva);
        publicadorReservaDeAtivo.publicarSeForAtivo(salva);
        return salva;
    }
}
