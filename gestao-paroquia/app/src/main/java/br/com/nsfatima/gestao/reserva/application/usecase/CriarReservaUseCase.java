package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService.PedidoReserva;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
import br.com.nsfatima.gestao.reserva.domain.model.IntervaloReserva;
import br.com.nsfatima.gestao.reserva.domain.model.Reserva;
import br.com.nsfatima.gestao.reserva.domain.model.StatusReserva;
import br.com.nsfatima.gestao.reserva.domain.model.TipoItemReserva;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService;
import br.com.nsfatima.gestao.reserva.domain.service.ReservaAuthorizationService.UsuarioActor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Pedido pontual de reserva de local ou ativo feito por um usuário em nome de uma pastoral ou movimento.
 *
 * <p>O status inicial vem exclusivamente da alçada do solicitante (RN-RES-03): a reserva nasce
 * {@code CONFIRMADA} para Pároco, administrador, secretaria paroquial e coordenador da pastoral, e
 * {@code PENDENTE} para os demais. Vínculo com evento ou série recorrente não é informável por este caminho:
 * só a integração com eventos e o caso de uso recorrente os definem.</p>
 */
@Service
public class CriarReservaUseCase {

    private final ReservaCriacaoService criacaoService;
    private final ReservaAuthorizationService authorizationService;

    public CriarReservaUseCase(
            ReservaCriacaoService criacaoService,
            ReservaAuthorizationService authorizationService
    ) {
        this.criacaoService = Objects.requireNonNull(criacaoService, "ReservaCriacaoService é obrigatório.");
        this.authorizationService = Objects.requireNonNull(authorizationService,
                "ReservaAuthorizationService é obrigatório.");
    }

    public record ComandoCriarReserva(
            TipoItemReserva tipoItem,
            UUID itemId,
            String itemNome,
            Instant dataHoraInicio,
            Instant dataHoraFim,
            Integer bufferMinutos,
            String descricao,
            UUID pastoralMovimentoId
    ) {}

    @Transactional
    public Reserva executar(ComandoCriarReserva comando, UsuarioActor solicitante) {
        Objects.requireNonNull(comando, "Comando de criação de reserva é obrigatório.");
        Objects.requireNonNull(solicitante, "Ator solicitante é obrigatório.");

        if (!solicitante.isMembroDaOrganizacao(comando.pastoralMovimentoId())) {
            throw new ReservaNaoAutorizadaException(
                    "O solicitante deve obrigatoriamente ter vínculo ativo com a pastoral/movimento selecionado.");
        }

        int buffer = comando.bufferMinutos() != null ? comando.bufferMinutos() : IntervaloReserva.BUFFER_PADRAO_MINUTOS;
        IntervaloReserva intervalo = new IntervaloReserva(comando.dataHoraInicio(), comando.dataHoraFim(), buffer);
        StatusReserva statusInicial =
                authorizationService.determinarStatusInicial(solicitante, comando.pastoralMovimentoId());

        return criacaoService.criar(new PedidoReserva(
                comando.tipoItem(),
                comando.itemId(),
                comando.itemNome(),
                intervalo,
                comando.descricao(),
                solicitante.id(),
                comando.pastoralMovimentoId(),
                null,
                null,
                statusInicial
        ));
    }
}
