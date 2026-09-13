package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.dto.DisponibilidadeFaixaDto;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaFiltro;
import br.com.nsfatima.gestao.reserva.domain.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class ObterDisponibilidadeItemUseCase {

    private static final ZoneId ZONA_PADRAO = ZoneId.of("America/Sao_Paulo");

    private final ReservaRepository reservaRepository;
    private final ConsultaManutencaoAtivoPort consultaManutencaoPort;

    public ObterDisponibilidadeItemUseCase(ReservaRepository reservaRepository,
                                           ConsultaManutencaoAtivoPort consultaManutencaoPort) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.consultaManutencaoPort = Objects.requireNonNull(consultaManutencaoPort,
                "ConsultaManutencaoAtivoPort é obrigatório.");
    }

    @Transactional(readOnly = true)
    public List<DisponibilidadeFaixaDto> executar(TipoItemReserva tipoItem, UUID itemId, LocalDate data) {
        Objects.requireNonNull(tipoItem, "Tipo de item é obrigatório.");
        Objects.requireNonNull(itemId, "ID do item é obrigatório.");
        LocalDate dia = data != null ? data : LocalDate.now(ZONA_PADRAO);

        Instant inicioDia = dia.atStartOfDay(ZONA_PADRAO).toInstant();
        Instant fimDia = dia.plusDays(1).atStartOfDay(ZONA_PADRAO).toInstant();

        ReservaFiltro filtro = new ReservaFiltro(
                tipoItem,
                itemId,
                null,
                null,
                null,
                null,
                inicioDia,
                fimDia,
                null
        );

        List<Reserva> reservas = reservaRepository.findAll(filtro);

        List<DisponibilidadeFaixaDto> faixas = new ArrayList<>(reservas.stream()
                .filter(r -> r.getStatus().ocupaAgenda())
                .map(r -> new DisponibilidadeFaixaDto(
                        r.getId().value(),
                        r.getIntervalo().dataHoraInicio(),
                        r.getIntervalo().dataHoraFim(),
                        r.getIntervalo().fimComBuffer(),
                        r.getIntervalo().bufferMinutos(),
                        r.getStatus(),
                        "Reservado",
                        r.getDescricao()
                ))
                .toList());

        // Bem em manutenção também bloqueia o calendário no intervalo da janela.
        if (tipoItem == TipoItemReserva.ATIVO) {
            for (ConsultaManutencaoAtivoPort.JanelaIndisponivel janela
                    : consultaManutencaoPort.janelasDeManutencao(itemId, inicioDia, fimDia)) {
                faixas.add(new DisponibilidadeFaixaDto(
                        null, janela.inicio(), janela.fim(), janela.fim(), 0,
                        StatusReserva.CONFIRMADA, "Em manutenção", "Ativo em manutenção"));
            }
        }

        return faixas;
    }
}
