package br.com.nsfatima.gestao.reserva.application.usecase;

import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaConflitoException.ConflitoDetalhe;
import br.com.nsfatima.gestao.reserva.domain.exception.ReservaNaoAutorizadaException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class CriarReservaRecorrenteUseCase {

    private final ReservaRepository reservaRepository;
    private final ReservaConflitoService conflitoService;
    private final ReservaAuthorizationService authorizationService;

    public CriarReservaRecorrenteUseCase(
            ReservaRepository reservaRepository,
            ReservaConflitoService conflitoService,
            ReservaAuthorizationService authorizationService
    ) {
        this.reservaRepository = Objects.requireNonNull(reservaRepository, "ReservaRepository é obrigatório.");
        this.conflitoService = Objects.requireNonNull(conflitoService, "ReservaConflitoService é obrigatório.");
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
        Instant agora = Instant.now();

        List<Reserva> candidatas = new ArrayList<>();
        List<ConflitoDetalhe> conflitosTotais = new ArrayList<>();

        for (OcorrenciaHorario ocorrencia : comando.ocorrencias()) {
            IntervaloReserva intervalo = new IntervaloReserva(
                    ocorrencia.dataHoraInicio(), ocorrencia.dataHoraFim(), buffer);
            List<Reserva> choques = conflitoService.buscarColisoesComLock(
                    comando.tipoItem(),
                    comando.itemId(),
                    intervalo,
                    null
            );

            if (!choques.isEmpty()) {
                for (Reserva choque : choques) {
                    conflitosTotais.add(new ConflitoDetalhe(
                            comando.itemId(),
                            comando.itemNome() != null ? comando.itemNome() : "Item " + comando.itemId(),
                            comando.tipoItem(),
                            choque.getIntervalo().dataHoraInicio(),
                            choque.getIntervalo().fimComBuffer(),
                            choque.getSolicitanteId(),
                            "Responsável",
                            choque.getDescricao()
                    ));
                }
            } else {
                candidatas.add(Reserva.criar(
                        ReservaId.gerar(),
                        comando.tipoItem(),
                        comando.itemId(),
                        intervalo,
                        comando.descricao(),
                        solicitante.id(),
                        comando.pastoralMovimentoId(),
                        null,
                        grupoId,
                        statusInicial,
                        agora
                ));
            }
        }

        if (!conflitosTotais.isEmpty() && !comando.salvarValidasDescartarConflitos()) {
            throw new ReservaConflitoException(
                    "Conflitos identificados na sequência da série de reservas recorrentes.",
                    conflitosTotais
            );
        }

        List<Reserva> salvas = candidatas.isEmpty() ? List.of() : reservaRepository.saveAll(candidatas);
        return new ResultadoRecorrencia(grupoId, salvas, conflitosTotais);
    }
}
