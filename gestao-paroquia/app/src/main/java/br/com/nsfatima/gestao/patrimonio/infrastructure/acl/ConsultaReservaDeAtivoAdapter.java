package br.com.nsfatima.gestao.patrimonio.infrastructure.acl;

import br.com.nsfatima.gestao.patrimonio.application.port.out.ConsultaReservaDeAtivoPort;
import br.com.nsfatima.gestao.patrimonio.domain.model.ReservaImpeditiva;
import br.com.nsfatima.gestao.reserva.application.service.ConsultaReservaDeAtivoService;
import br.com.nsfatima.gestao.reserva.application.service.ReservaImpeditivaView;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Anti-Corruption Layer: implementa a porta de patrimônio delegando à API pública do módulo de
 * Reservas e traduzindo {@link ReservaImpeditivaView} para o conceito local {@link ReservaImpeditiva}.
 */
@Component
public class ConsultaReservaDeAtivoAdapter implements ConsultaReservaDeAtivoPort {

    private final ConsultaReservaDeAtivoService consultaReservaService;

    public ConsultaReservaDeAtivoAdapter(ConsultaReservaDeAtivoService consultaReservaService) {
        this.consultaReservaService = consultaReservaService;
    }

    @Override
    public boolean temReservaAtivaAgora(UUID ativoId) {
        return consultaReservaService.temReservaAtivaAgora(ativoId);
    }

    @Override
    public List<ReservaImpeditiva> reservasImpeditivas(UUID ativoId, Instant inicio, Instant fim) {
        return consultaReservaService.reservasImpeditivas(ativoId, inicio, fim).stream()
                .map(this::traduzir)
                .toList();
    }

    @Override
    public boolean temReservaFuturaImpeditiva(UUID ativoId) {
        return consultaReservaService.temReservaFuturaImpeditiva(ativoId);
    }

    private ReservaImpeditiva traduzir(ReservaImpeditivaView v) {
        return new ReservaImpeditiva(
                v.reservaId(), v.descricao(), v.inicio(), v.fim(), v.status(), v.solicitanteNome());
    }
}
