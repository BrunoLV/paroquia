package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.event.PublicadorReservaDeAtivo;
import br.com.nsfatima.gestao.reserva.application.port.out.ConsultaManutencaoAtivoPort;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaOperacaoInvalidaException;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.ReservaId;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
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
public class CriarReservaUseCase {

    private final ReservaRepository reservaRepository;
    private final ReservaConflitoService conflitoService;
    private final ReservaAuthorizationService authorizationService;
    private final ConsultaManutencaoAtivoPort consultaManutencaoPort;
    private final PublicadorReservaDeAtivo publicadorReservaDeAtivo;

    public CriarReservaUseCase(
            ReservaRepository reservaRepository,
            ReservaConflitoService conflitoService,
            ReservaAuthorizationService authorizationService,
            ConsultaManutencaoAtivoPort consultaManutencaoPort,
            PublicadorReservaDeAtivo publicadorReservaDeAtivo
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.conflitoService = Objects.requireNonNull(conflitoService, "ReservaConflitoService é obrigatório.");
        this.authorizationService = Objects.requireNonNull(authorizationService,
                "ReservaAuthorizationService é obrigatório.");
        this.consultaManutencaoPort = Objects.requireNonNull(consultaManutencaoPort,
                "ConsultaManutencaoAtivoPort é obrigatório.");
        this.publicadorReservaDeAtivo = Objects.requireNonNull(publicadorReservaDeAtivo,
                "PublicadorReservaDeAtivo é obrigatório.");
    }

    public record ComandoCriarReserva(
            TipoItemReserva tipoItem,
            UUID itemId,
            String itemNome,
            Instant dataHoraInicio,
            Instant dataHoraFim,
            Integer bufferMinutos,
            String descricao,
            UUID pastoralMovimentoId,
            UUID eventoId,
            UUID grupoRecorrenciaId
    ) {}

    @Transactional
    public Reserva executar(ComandoCriarReserva comando, UsuarioActor solicitante) {
        Objects.requireNonNull(comando, "Comando de criação de reserva é obrigatório.");
        Objects.requireNonNull(solicitante, "Ator solicitante é obrigatório.");

        // Validação de vínculo de pastoral
        if (!solicitante.isMembroDaOrganizacao(comando.pastoralMovimentoId())) {
            throw new ReservaNaoAutorizadaException("O solicitante deve obrigatoriamente ter vínculo ativo com a pastoral/movimento selecionado.");
        }

        int buffer = comando.bufferMinutos() != null ? comando.bufferMinutos() : IntervaloReserva.BUFFER_PADRAO_MINUTOS;
        IntervaloReserva intervalo = new IntervaloReserva(comando.dataHoraInicio(), comando.dataHoraFim(), buffer);

        // Validação de conflito com bloqueio pessimista
        conflitoService.validarDisponibilidadeComLock(
                comando.tipoItem(),
                comando.itemId(),
                comando.itemNome(),
                intervalo,
                null
        );

        // Bem em manutenção bloqueia o calendário de reservas no intervalo da janela.
        if (comando.tipoItem() == TipoItemReserva.ATIVO
                && !consultaManutencaoPort.janelasDeManutencao(
                        comando.itemId(), comando.dataHoraInicio(), comando.dataHoraFim()).isEmpty()) {
            throw new ReservaOperacaoInvalidaException(
                    "O ativo está em manutenção (ou com manutenção prevista) no período solicitado.");
        }

        // Status inicial: se for criado via Evento oficial ou solicitante for Admin/Coord -> CONFIRMADA, senão PENDENTE
        StatusReserva statusInicial = comando.eventoId() != null
                ? StatusReserva.CONFIRMADA
                : authorizationService.determinarStatusInicial(solicitante, comando.pastoralMovimentoId());

        Instant agora = Instant.now();
        Reserva reserva = Reserva.criar(
                ReservaId.generate(),
                comando.tipoItem(),
                comando.itemId(),
                intervalo,
                comando.descricao(),
                solicitante.id(),
                comando.pastoralMovimentoId(),
                comando.eventoId(),
                comando.grupoRecorrenciaId(),
                statusInicial,
                agora
        );

        Reserva salva = reservaRepository.save(reserva);
        if (salva.getStatus() == StatusReserva.CONFIRMADA) {
            publicadorReservaDeAtivo.publicarSeForAtivo(salva);
        }
        return salva;
    }
}
