package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService;
import br.com.nsfatima.gestao.reserva.application.service.ReservaCriacaoService.PedidoReserva;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Série de reservas recorrentes de um mesmo item (RN-RES-07). Cada ocorrência passa pelas mesmas validações de
 * uma reserva avulsa. Ocorrências são gravadas à medida que são aceitas, de modo que uma ocorrência da série
 * também conflita com as anteriores; se houver choques e o chamador não pediu para descartá-los, a transação
 * inteira é desfeita e o conflito é informado.
 */
@Service
public class CriarReservaRecorrenteUseCase {

    private final ReservaCriacaoService criacaoService;
    private final ReservaAuthorizationService authorizationService;

    public CriarReservaRecorrenteUseCase(
            ReservaCriacaoService criacaoService,
            ReservaAuthorizationService authorizationService
    ) {
        this.criacaoService = Objects.requireNonNull(criacaoService, "ReservaCriacaoService é obrigatório.");
        this.authorizationService = Objects.requireNonNull(authorizationService,
                "ReservaAuthorizationService é obrigatório.");
    }

    public record OcorrenciaHorario(
            Instant dataHoraInicio,
            Instant dataHoraFim
    ) {}

    public record ComandoCriarRecorrente(
            TipoItemReserva tipoItem,
            UUID itemId,
            String itemNome,
            Integer bufferMinutos,
            String descricao,
            UUID pastoralMovimentoId,
            List<OcorrenciaHorario> ocorrencias,
            boolean salvarValidasDescartarConflitos
    ) {}

    public record ResultadoRecorrencia(
            UUID grupoRecorrenciaId,
            List<Reserva> reservasCriadas,
            List<ConflitoDetalhe> conflitosIdentificados
    ) {}

    @Transactional
    public ResultadoRecorrencia executar(ComandoCriarRecorrente comando, UsuarioActor solicitante) {
        Objects.requireNonNull(comando, "Comando recorrente é obrigatório.");
        Objects.requireNonNull(solicitante, "Solicitante é obrigatório.");

        if (!solicitante.isMembroDaOrganizacao(comando.pastoralMovimentoId())) {
            throw new ReservaNaoAutorizadaException("O solicitante deve ter vínculo com a pastoral/movimento.");
        }

        if (comando.ocorrencias() == null || comando.ocorrencias().isEmpty()) {
            throw new IllegalArgumentException("A série recorrente deve conter ao menos uma ocorrência.");
        }

        UUID grupoId = UUID.randomUUID();
        int buffer = comando.bufferMinutos() != null ? comando.bufferMinutos() : IntervaloReserva.BUFFER_PADRAO_MINUTOS;
        StatusReserva statusInicial = authorizationService.determinarStatusInicial(
                solicitante, comando.pastoralMovimentoId());

        List<Reserva> criadas = new ArrayList<>();
        List<ConflitoDetalhe> conflitosTotais = new ArrayList<>();

        for (OcorrenciaHorario ocorrencia : comando.ocorrencias()) {
            PedidoReserva pedido = new PedidoReserva(
                    comando.tipoItem(),
                    comando.itemId(),
                    comando.itemNome(),
                    new IntervaloReserva(ocorrencia.dataHoraInicio(), ocorrencia.dataHoraFim(), buffer),
                    comando.descricao(),
                    solicitante.id(),
                    comando.pastoralMovimentoId(),
                    null,
                    grupoId,
                    statusInicial
            );
            List<ConflitoDetalhe> impedimentos = new ArrayList<>(criacaoService.conflitosDeAgenda(pedido));
            criacaoService.janelaDeManutencao(pedido).ifPresent(impedimentos::add);

            if (impedimentos.isEmpty()) {
                criadas.add(criacaoService.registrar(pedido));
            } else {
                conflitosTotais.addAll(impedimentos);
            }
        }

        if (!conflitosTotais.isEmpty() && !comando.salvarValidasDescartarConflitos()) {
            throw new ReservaConflitoException(
                    "Conflitos identificados na sequência da série de reservas recorrentes.",
                    conflitosTotais
            );
        }

        return new ResultadoRecorrencia(grupoId, criadas, conflitosTotais);
    }
}
