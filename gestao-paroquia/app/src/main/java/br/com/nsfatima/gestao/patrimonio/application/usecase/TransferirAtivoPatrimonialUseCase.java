package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.TransferirAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoTransferenciaBloqueadaException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoGrupoParoquialPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;

/**
 * Transfere a posse de um ativo patrimonial para outra organização. A transferência é bloqueada
 * enquanto o ativo estiver {@code EM_MANUTENCAO} ou {@code ALOCADO}, pois trocar a organização
 * dona nessas condições invalidaria a alocação/manutenção corrente; a organização de destino
 * também deve ser de um tipo elegível a possuir patrimônio ({@code AtivoGrupoParoquialPolicy}).
 */
@Service
public class TransferirAtivoPatrimonialUseCase {

    private static final Set<AtivoStatus> STATUS_BLOQUEADOS_PARA_TRANSFERENCIA =
            Set.of(AtivoStatus.EM_MANUTENCAO, AtivoStatus.ALOCADO);

    private final AtivoPatrimonialJpaRepository repository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final AtivoGrupoParoquialPolicy grupoParoquialPolicy;
    private final PatrimonioAuditPublisher auditPublisher;

    public TransferirAtivoPatrimonialUseCase(AtivoPatrimonialJpaRepository repository,
                                              EventoActorContextResolver actorContextResolver,
                                              PatrimonioAuthorizationService authorizationService,
                                              AtivoGrupoParoquialPolicy grupoParoquialPolicy,
                                              PatrimonioAuditPublisher auditPublisher) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.grupoParoquialPolicy = grupoParoquialPolicy;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Transfere o ativo para a organização de destino, validando que o status atual permite
     * transferência e que a organização de destino é elegível como proprietária de patrimônio.
     *
     * @param ativoId ativo a transferir
     * @param request organização de destino
     * @return o ativo com a organização dona já atualizada
     * @throws AtivoPatrimonialNotFoundException    se o ativo não existir
     * @throws AtivoTransferenciaBloqueadaException se o ativo estiver em manutenção ou alocado
     */
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
        authorizationService.assertCanTransfer(actorContext, ativo.getGrupoParoquialId());
        grupoParoquialPolicy.assertGrupoParoquialProprietariaPermitida(request.novaGrupoParoquialId());

        UUID grupoParoquialOrigemId = ativo.getGrupoParoquialId();
        ativo.setGrupoParoquialId(request.novaGrupoParoquialId());
        AtivoPatrimonialEntity saved = repository.save(ativo);

        auditPublisher.publishTransfer(actorContext.actor(), saved.getId(), grupoParoquialOrigemId,
                request.novaGrupoParoquialId());
        return CreateAtivoPatrimonialUseCase.toResponse(saved);
    }
}
