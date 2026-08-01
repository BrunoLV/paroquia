package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.TransferirAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoTransferenciaBloqueadaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoOrganizacaoPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

@Service
public class TransferirAtivoPatrimonialUseCase {

    private static final Set<AtivoStatus> STATUS_BLOQUEADOS_PARA_TRANSFERENCIA =
            Set.of(AtivoStatus.EM_MANUTENCAO, AtivoStatus.ALOCADO);

    private final AtivoPatrimonialJpaRepository repository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final AtivoOrganizacaoPolicy organizacaoPolicy;
    private final PatrimonioAuditPublisher auditPublisher;

    public TransferirAtivoPatrimonialUseCase(AtivoPatrimonialJpaRepository repository,
                                              EventoActorContextResolver actorContextResolver,
                                              PatrimonioAuthorizationService authorizationService,
                                              AtivoOrganizacaoPolicy organizacaoPolicy,
                                              PatrimonioAuditPublisher auditPublisher) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.organizacaoPolicy = organizacaoPolicy;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    public AtivoPatrimonialResponse execute(UUID ativoId, TransferirAtivoRequest request) {
        AtivoPatrimonialEntity ativo = repository.findById(ativoId)
                .orElseThrow(() -> new AtivoPatrimonialNotFoundException(
                        "Ativo patrimonial não encontrado: " + ativoId));

        if (STATUS_BLOQUEADOS_PARA_TRANSFERENCIA.contains(ativo.getStatus())) {
            throw new AtivoTransferenciaBloqueadaException(
                    "Não é possível transferir um ativo com status " + ativo.getStatus());
        }

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanTransfer(actorContext, ativo.getOrganizacaoId());
        organizacaoPolicy.assertOwnerOrganizacaoPermitida(request.novaOrganizacaoId());

        UUID organizacaoOrigemId = ativo.getOrganizacaoId();
        ativo.setOrganizacaoId(request.novaOrganizacaoId());
        AtivoPatrimonialEntity saved = repository.save(ativo);

        auditPublisher.publishTransfer(actorContext.actor(), saved.getId(), organizacaoOrigemId,
                request.novaOrganizacaoId());
        return CreateAtivoPatrimonialUseCase.toResponse(saved);
    }
}
