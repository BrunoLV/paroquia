package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoManutencaoResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.EnviarParaManutencaoRequest;
import br.com.nsfatima.gestao.patrimonio.application.port.out.ConsultaReservaDeAtivoPort;
import br.com.nsfatima.gestao.patrimonio.domain.event.AtivoEnviadoParaManutencaoEvent;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoConflitanteException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNaoEncontradoException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencao;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencaoId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.PeriodoManutencao;
import br.com.nsfatima.gestao.patrimonio.domain.model.ReservaImpeditiva;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoManutencaoRepository;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.patrimonio.application.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Envia um ativo {@code DISPONIVEL} para manutenção por um período determinado. O envio é bloqueado
 * se houver reservas {@code PENDENTE}/{@code CONFIRMADA} no intervalo (que devem ser canceladas
 * antes) ou se já houver uma manutenção em aberto. Uma vez registrada, a janela bloqueia o
 * calendário de reservas.
 */
@Service
public class EnviarAtivoParaManutencaoUseCase {

    private final AtivoPatrimonialRepository ativoRepository;
    private final AtivoManutencaoRepository manutencaoRepository;
    private final ConsultaReservaDeAtivoPort consultaReservaPort;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final PatrimonioAuditPublisher auditPublisher;
    private final ApplicationEventPublisher eventPublisher;

    public EnviarAtivoParaManutencaoUseCase(AtivoPatrimonialRepository ativoRepository,
                                            AtivoManutencaoRepository manutencaoRepository,
                                            ConsultaReservaDeAtivoPort consultaReservaPort,
                                            EventoActorContextResolver actorContextResolver,
                                            PatrimonioAuthorizationService authorizationService,
                                            PatrimonioAuditPublisher auditPublisher,
                                            ApplicationEventPublisher eventPublisher) {
        this.ativoRepository = ativoRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.consultaReservaPort = consultaReservaPort;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.auditPublisher = auditPublisher;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AtivoManutencaoResponse execute(UUID ativoId, EnviarParaManutencaoRequest request) {
        AtivoPatrimonial ativo = carregar(ativoId);

        EventoActorContext actor = actorContextResolver.resolveRequired();
        authorizationService.assertPodeGerirPatrimonio(actor, ativo.getGrupoParoquialId());

        PeriodoManutencao periodo = new PeriodoManutencao(request.dataInicio(), request.dataFimPrevista());

        if (manutencaoRepository.existeEmAbertoPorAtivo(ativo.getId())) {
            throw new AtivoManutencaoInvalidaException("Já existe uma manutenção em aberto para este ativo.");
        }

        List<ReservaImpeditiva> impeditivas =
                consultaReservaPort.reservasImpeditivas(ativoId, request.dataInicio(), request.dataFimPrevista());
        if (!impeditivas.isEmpty()) {
            throw new AtivoManutencaoConflitanteException(impeditivas);
        }

        Instant agora = Instant.now();
        ativo.enviarParaManutencao(agora);

        AtivoManutencao manutencao = AtivoManutencao.registrar(
                AtivoManutencaoId.generate(), ativo.getId(), request.descricaoReparo(), periodo,
                actor.usuarioId(), agora);
        AtivoManutencao salva = manutencaoRepository.save(manutencao);
        ativoRepository.save(ativo);

        auditPublisher.publishManutencao(actor.actor(), ativoId, ativo.getGrupoParoquialId());
        eventPublisher.publishEvent(new AtivoEnviadoParaManutencaoEvent(
                ativoId, request.dataInicio(), request.dataFimPrevista()));

        return AtivoManutencaoResponse.de(salva);
    }

    private AtivoPatrimonial carregar(UUID ativoId) {
        AtivoPatrimonial ativo = ativoRepository.findById(new AtivoPatrimonialId(ativoId))
                .orElseThrow(() -> new AtivoPatrimonialNaoEncontradoException("Ativo não encontrado: " + ativoId));
        if (ativo.estaExcluido()) {
            throw new AtivoPatrimonialNaoEncontradoException("Ativo não encontrado: " + ativoId);
        }
        return ativo;
    }
}
