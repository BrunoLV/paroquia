package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.domain.exception.EventoNotFoundException;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.local.infrastructure.persistence.entity.LocalEntity;
import br.com.nsfatima.gestao.local.infrastructure.persistence.repository.LocalJpaRepository;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AlocarAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.application.usecase.observacao.CreateNotaAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoAlocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoVinculacaoPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AlocarAtivoParaEventoUseCase {

    private static final String TIPO_NOTA_ALOCACAO = "ALOCACAO";

    private final AtivoPatrimonialJpaRepository ativoRepository;
    private final EventoJpaRepository eventoRepository;
    private final LocalJpaRepository localRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final AtivoVinculacaoPolicy vinculacaoPolicy;
    private final CreateNotaAtivoPatrimonialUseCase createNotaUseCase;
    private final PatrimonioAuditPublisher auditPublisher;

    public AlocarAtivoParaEventoUseCase(AtivoPatrimonialJpaRepository ativoRepository,
                                        EventoJpaRepository eventoRepository,
                                        LocalJpaRepository localRepository,
                                        EventoActorContextResolver actorContextResolver,
                                        PatrimonioAuthorizationService authorizationService,
                                        AtivoVinculacaoPolicy vinculacaoPolicy,
                                        CreateNotaAtivoPatrimonialUseCase createNotaUseCase,
                                        PatrimonioAuditPublisher auditPublisher) {
        this.ativoRepository = ativoRepository;
        this.eventoRepository = eventoRepository;
        this.localRepository = localRepository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.vinculacaoPolicy = vinculacaoPolicy;
        this.createNotaUseCase = createNotaUseCase;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    public AtivoPatrimonialResponse execute(UUID ativoId, AlocarAtivoRequest request) {
        AtivoPatrimonialEntity ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new AtivoPatrimonialNotFoundException(
                        "Ativo patrimonial não encontrado: " + ativoId));

        EventoEntity evento = eventoRepository.findById(request.eventoId())
                .orElseThrow(() -> new EventoNotFoundException(request.eventoId()));

        LocalEntity local = localRepository.findById(evento.getLocalId())
                .orElseThrow(() -> new AtivoAlocacaoInvalidaException("Local não encontrado: " + evento.getLocalId()));

        vinculacaoPolicy.assertCanAlocar(ativo, local.getOrganizacaoId(), request.justificativaAlocacao());

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanAlocar(actorContext, ativo.getOrganizacaoId());

        ativo.setStatus(AtivoStatus.ALOCADO);
        AtivoPatrimonialEntity saved = ativoRepository.save(ativo);

        createNotaUseCase.execute(saved.getId(), actorContext.usuarioId(), TIPO_NOTA_ALOCACAO,
                request.justificativaAlocacao());

        auditPublisher.publishAlocacao(actorContext.actor(), saved.getId(), saved.getOrganizacaoId(),
                request.eventoId());
        return CreateAtivoPatrimonialUseCase.toResponse(saved);
    }
}
