package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.fiel.application.usecase.GerenciarNotificacaoUseCase;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Varredura de expiração de reservas pendentes (RN-RES-05): o pedido não analisado até 48 horas antes do início
 * expira, liberando a agenda e notificando o solicitante. Pedido feito com menos de 48 horas de antecedência
 * só expira no início (ver {@link Reserva#prazoDeAnalise()}), nunca no instante em que nasce.
 *
 * <p>Não é transacional: cada reserva é expirada por {@link ExpirarReservaPendenteUseCase} em transação própria
 * e a notificação ocorre depois do commit, de modo que nem a falha de uma reserva nem a de uma notificação
 * desfazem as demais expirações.</p>
 */
@Service
public class ExpirarReservasPendentesUseCase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExpirarReservasPendentesUseCase.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
            .withZone(ZoneId.of("America/Sao_Paulo"));

    private final ReservaRepository reservaRepository;
    private final ExpirarReservaPendenteUseCase expirarReservaPendenteUseCase;
    private final GerenciarNotificacaoUseCase notificacaoUseCase;

    public ExpirarReservasPendentesUseCase(
            ReservaRepository reservaRepository,
            ExpirarReservaPendenteUseCase expirarReservaPendenteUseCase,
            GerenciarNotificacaoUseCase notificacaoUseCase
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.expirarReservaPendenteUseCase = Objects.requireNonNull(expirarReservaPendenteUseCase,
                "ExpirarReservaPendenteUseCase é obrigatório.");
        this.notificacaoUseCase = Objects.requireNonNull(notificacaoUseCase,
                "GerenciarNotificacaoUseCase é obrigatório.");
    }

    public int executar() {
        Instant agora = Instant.now();
        Instant limite = agora.plus(Reserva.ANTECEDENCIA_ANALISE);

        List<Reserva> candidatas = reservaRepository.findPendentesExpiracao(limite);
        int expiradas = 0;
        for (Reserva candidata : candidatas) {
            if (candidata.analiseVencida(agora) && expirar(candidata, agora)) {
                expiradas++;
            }
        }
        return expiradas;
    }

    private boolean expirar(Reserva candidata, Instant agora) {
        try {
            if (expirarReservaPendenteUseCase.executar(candidata.getId().value(), agora).isEmpty()) {
                return false;
            }
        } catch (RuntimeException ex) {
            LOGGER.error("Falha ao expirar a reserva {}: {}", candidata.getId().value(), ex.getMessage(), ex);
            return false;
        }
        notificar(candidata);
        return true;
    }

    private void notificar(Reserva reserva) {
        String mensagem = String.format(
                "Sua solicitação de reserva para %s (%s) expirou automaticamente por não ter sido avaliada "
                        + "dentro do prazo de análise.",
                reserva.getTipoItem().name(),
                FORMATTER.format(reserva.getIntervalo().dataHoraInicio())
        );
        try {
            notificacaoUseCase.publicar(reserva.getSolicitanteId(), mensagem);
        } catch (RuntimeException ex) {
            LOGGER.warn("Reserva {} expirada, mas a notificação ao solicitante falhou: {}",
                    reserva.getId().value(), ex.getMessage());
        }
    }
}
