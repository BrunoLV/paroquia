package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.application.port.out.ConsultaReservaDeAtivoPort;
import br.com.nsfatima.gestao.patrimonio.domain.event.AtivoExcluidoEvent;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoExclusaoBloqueadaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoManutencaoRepository;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.patrimonio.application.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Exclusão lógica (soft delete) de um ativo — desincorporação patrimonial. Bloqueada se houver
 * manutenção em aberto ou reservas futuras {@code PENDENTE}/{@code CONFIRMADA}.
 */
@Service
public class ExcluirAtivoPatrimonialUseCase {

    private final AtivoPatrimonialRepository ativoRepository;
    private final AtivoManutencaoRepository manutencaoRepository;
    private final ConsultaReservaDeAtivoPort consultaReservaPort;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final PatrimonioAuditPublisher auditPublisher;
    private final ApplicationEventPublisher eventPublisher;

    public ExcluirAtivoPatrimonialUseCase(AtivoPatrimonialRepository ativoRepository,
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
    public void execute(UUID ativoId) {
        AtivoPatrimonial ativo = ativoRepository.findById(new AtivoPatrimonialId(ativoId))
                .filter(a -> !a.estaExcluido())
                .orElseThrow(() -> new AtivoPatrimonialNotFoundException("Ativo não encontrado: " + ativoId));

        EventoActorContext actor = actorContextResolver.resolveRequired();
        authorizationService.assertPodeGerirPatrimonio(actor, ativo.getGrupoParoquialId());

        if (manutencaoRepository.existeEmAbertoPorAtivo(ativo.getId())) {
            throw new AtivoExclusaoBloqueadaException(
                    "O ativo não pode ser excluído: há manutenção em aberto.");
        }
        if (consultaReservaPort.temReservaFuturaImpeditiva(ativoId)) {
            throw new AtivoExclusaoBloqueadaException(
                    "O ativo não pode ser excluído: há reservas futuras confirmadas ou pendentes.");
        }

        ativo.excluir(Instant.now());
        ativoRepository.save(ativo);

        auditPublisher.publishExclusao(actor.actor(), ativoId, ativo.getGrupoParoquialId());
        eventPublisher.publishEvent(new AtivoExcluidoEvent(ativoId));
    }
}
