package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.calendario.application.usecase.observacao.RegisterSystemObservacaoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoObservacaoInput;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNotFoundException;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.UUID;

@Service
public class CancelarReservaUseCase {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    private final ReservaRepository reservaRepository;
    private final ReservaAuthorizationService authorizationService;
    private final RegisterSystemObservacaoUseCase registerSystemObservacaoUseCase;

    public CancelarReservaUseCase(
            ReservaRepository reservaRepository,
            ReservaAuthorizationService authorizationService,
            RegisterSystemObservacaoUseCase registerSystemObservacaoUseCase
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.authorizationService = Objects.requireNonNull(authorizationService, "ReservaAuthorizationService é obrigatório.");
        this.registerSystemObservacaoUseCase = Objects.requireNonNull(registerSystemObservacaoUseCase, "RegisterSystemObservacaoUseCase é obrigatório.");
    }

    @Transactional
    public Reserva executar(UUID reservaId, String motivo, String itemNome, UsuarioActor operador) {
        Objects.requireNonNull(reservaId, "ID da reserva é obrigatório.");
        Objects.requireNonNull(operador, "Operador é obrigatório.");

        Reserva reserva = reservaRepository.findById(new ReservaId(reservaId))
                .orElseThrow(() -> new ReservaNotFoundException(reservaId));

        Instant agora = Instant.now();
        authorizationService.validarAutorizacaoCancelamento(operador, reserva, agora);

        boolean isAdmin = operador.isAdminOuParoco();
        reserva.cancelar(operador.id(), motivo, isAdmin, agora);
        Reserva salva = reservaRepository.save(reserva);

        // Regra 4.4: Se vinculado a evento, injetar observação no evento-pai
        if (reserva.getEventoId() != null) {
            String nomeRecurso = itemNome != null && !itemNome.isBlank()
                    ? itemNome
                    : reserva.getTipoItem().name() + " " + reserva.getItemId();

            String obsConteudo = String.format(
                    "Reserva de [%s: %s] cancelada por [%s] em [%s]. Motivo: [%s]",
                    reserva.getTipoItem().name(),
                    nomeRecurso,
                    operador.nome() != null ? operador.nome() : "Usuário " + operador.id(),
                    FORMATTER.format(agora),
                    motivo.trim()
            );

            registerSystemObservacaoUseCase.execute(
                    reserva.getEventoId(),
                    TipoObservacaoInput.CANCELAMENTO,
                    obsConteudo,
                    operador.id(),
                    operador.nome(),
                    "cancelamento-avulso-reserva"
            );
        }

        return salva;
    }
}
