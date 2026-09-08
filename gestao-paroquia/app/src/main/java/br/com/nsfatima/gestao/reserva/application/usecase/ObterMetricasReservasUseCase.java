package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.dto.MetricasReservasDto;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ObterMetricasReservasUseCase {

    private final ReservaRepository reservaRepository;

    public ObterMetricasReservasUseCase(ReservaRepository reservaRepository) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
    }

    @Transactional(readOnly = true)
    public MetricasReservasDto executar(UUID pastoralId) {
        ZoneId zone = ZoneId.of("America/Sao_Paulo");
        LocalDate hoje = LocalDate.now(zone);
        Instant inicioHoje = hoje.atStartOfDay(zone).toInstant();
        Instant fimHoje = hoje.plusDays(1).atStartOfDay(zone).toInstant();

        YearMonth mesAtual = YearMonth.from(hoje);
        Instant inicioMes = mesAtual.atDay(1).atStartOfDay(zone).toInstant();
        Instant fimMes = mesAtual.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant();

        long pendentes = reservaRepository.countPendentesAprovacao(pastoralId);
        long devolucoesPendentes = reservaRepository.countDevolucoesPendentesHoje(inicioHoje, fimHoje);

        ReservaFiltro filtroHoje = new ReservaFiltro(null, null, null, pastoralId, null, null, inicioHoje, fimHoje, null);
        List<Reserva> reservasHoje = reservaRepository.findAll(filtroHoje);
        long ativasHoje = reservasHoje.stream().filter(r -> r.getStatus().ocupaAgenda()).count();

        ReservaFiltro filtroMes = new ReservaFiltro(null, null, null, pastoralId, null, null, inicioMes, fimMes, null);
        long totalMes = reservaRepository.findAll(filtroMes).size();

        return new MetricasReservasDto(pendentes, devolucoesPendentes, ativasHoje, totalMes);
    }
}
