package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Expiração preventiva (TTL) de reservas pendentes:
 * Solicitações pendentes não analisadas até 48h antes do início são expiradas automaticamente,
 * liberando a agenda e notificando o solicitante.
 */
@Service
public class ExpirarReservasPendentesUseCase {

    private static final Duration TTL_ANTECEDENCIA = Duration.ofHours(48);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    private final ReservaRepository reservaRepository;
    private final GerenciarNotificacaoUseCase notificacaoUseCase;
    private final PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    public ExpirarReservasPendentesUseCase(
            ReservaRepository reservaRepository,
            GerenciarNotificacaoUseCase notificacaoUseCase,
            PublicadorReservaDeAtivo publicadorReservaDeAtivo
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.notificacaoUseCase = Objects.requireNonNull(notificacaoUseCase,
                "GerenciarNotificacaoUseCase é obrigatório.");
        this.publicadorReservaDeAtivo = Objects.requireNonNull(publicadorReservaDeAtivo,
                "PublicadorReservaDeAtivo é obrigatório.");
    }

    @Transactional
    public int executar() {
        Instant agora = Instant.now();
        Instant limite = agora.plus(TTL_ANTECEDENCIA);

        List<Reserva> pendentes = reservaRepository.findPendentesExpiracao(limite);
        for (Reserva r : pendentes) {
            r.expirar(agora);
            Reserva salva = reservaRepository.save(r);
            publicadorReservaDeAtivo.publicarSeForAtivo(salva);

            String dataFormatada = FORMATTER.format(r.getIntervalo().dataHoraInicio());
            String mensagem = String.format(
                    "Sua solicitação de reserva para %s (%s) expirou automaticamente por não ter sido avaliada até 48h antes.",
                    r.getTipoItem().name(),
                    dataFormatada
            );
            try {
                notificacaoUseCase.publicar(r.getSolicitanteId(), mensagem);
            } catch (Exception ignored) {
                // Notificação não deve quebrar a transação de expiração
            }
        }
        return pendentes.size();
    }
}
