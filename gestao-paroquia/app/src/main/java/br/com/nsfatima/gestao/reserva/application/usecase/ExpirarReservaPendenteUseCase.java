package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Expira uma única reserva pendente em transação própria. Existe separado da varredura para que cada expiração
 * confirme ou desfaça isoladamente: a falha de uma reserva não reverte as já expiradas.
 *
 * <p>A reserva é recarregada e o prazo reavaliado dentro da transação, pois ela pode ter sido aprovada,
 * recusada ou cancelada entre a varredura e a expiração; nesse caso nada é alterado.</p>
 */
@Service
public class ExpirarReservaPendenteUseCase {

    private final ReservaRepository reservaRepository;
    private final PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    public ExpirarReservaPendenteUseCase(
            ReservaRepository reservaRepository,
            PublicadorReservaDeAtivo publicadorReservaDeAtivo
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.publicadorReservaDeAtivo = Objects.requireNonNull(publicadorReservaDeAtivo,
                "PublicadorReservaDeAtivo é obrigatório.");
    }

    /**
     * Expira a reserva se ela ainda estiver pendente e com o prazo de análise vencido em {@code agora}.
     *
     * @return a reserva expirada, ou vazio se ela deixou de estar pendente ou o prazo ainda não venceu
     */
    @Transactional
    public Optional<Reserva> executar(UUID reservaId, Instant agora) {
        return reservaRepository.findById(new ReservaId(reservaId))
                .filter(r -> r.analiseVencida(agora))
                .map(r -> {
                    r.expirar(agora);
                    Reserva salva = reservaRepository.save(r);
                    publicadorReservaDeAtivo.publicarSeForAtivo(salva);
                    return salva;
                });
    }
}
