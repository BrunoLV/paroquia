package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.DesalocarAtivoRequest;
import br.com.nsfatima.gestao.patrimonio.application.usecase.observacao.CreateNotaAtivoPatrimonialUseCase;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoDesalocacaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.TipoNotaAtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoAlocacaoEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoAlocacaoJpaRepository;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository.AtivoPatrimonialJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Encerra a ocupação corrente de um ativo alocado a um evento, marcando a alocação como
 * {@code DEVOLVIDA} e retornando o ativo ao status {@code DISPONIVEL} para a organização dona.
 */
@Service
public class DesalocarAtivoUseCase {

    private static final List<AtivoAlocacaoStatus> STATUS_DESALOCAVEIS = List.of(
            AtivoAlocacaoStatus.CONFIRMADA, AtivoAlocacaoStatus.ATRASADA);

    private final AtivoPatrimonialJpaRepository ativoRepository;
    private final AtivoAlocacaoJpaRepository alocacaoRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final CreateNotaAtivoPatrimonialUseCase createNotaUseCase;
    private final PatrimonioAuditPublisher auditPublisher;

    public DesalocarAtivoUseCase(AtivoPatrimonialJpaRepository ativoRepository,
                                  AtivoAlocacaoJpaRepository alocacaoRepository,
                                  EventoActorContextResolver actorContextResolver,
                                  PatrimonioAuthorizationService authorizationService,
                                  CreateNotaAtivoPatrimonialUseCase createNotaUseCase,
                                  PatrimonioAuditPublisher auditPublisher) {
        this.ativoRepository = ativoRepository;
        this.alocacaoRepository = alocacaoRepository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.createNotaUseCase = createNotaUseCase;
        this.auditPublisher = auditPublisher;
    }

    /**
     * Desaloca o ativo, procurando entre suas ocupações a alocação de evento corrente (status
     * {@code CONFIRMADA} ou {@code ATRASADA}) e registrando a devolução com uma nota obrigatória.
     *
     * @param ativoId ativo a desalocar
     * @param request observação da devolução (opcional; usa texto padrão se omitida)
     * @return o ativo já com status {@code DISPONIVEL}
     * @throws AtivoPatrimonialNotFoundException  se o ativo não existir
     * @throws AtivoDesalocacaoInvalidaException se não houver alocação de evento corrente
     */
    @Transactional
    public AtivoPatrimonialResponse execute(UUID ativoId, DesalocarAtivoRequest request) {
        AtivoPatrimonialEntity ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new AtivoPatrimonialNotFoundException(
                        "Ativo patrimonial não encontrado: " + ativoId));

        AtivoAlocacaoEntity ocupacao = alocacaoRepository.findByAtivoIdAndStatusIn(ativoId, STATUS_DESALOCAVEIS)
                .stream()
                .filter(existente -> existente.getTipo() == AtivoAlocacaoTipo.ALOCACAO_EVENTO)
                .findFirst()
                .orElseThrow(() -> new AtivoDesalocacaoInvalidaException(
                        "O ativo não possui uma alocação de evento corrente para desalocar"));

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanDesalocar(actorContext, ativo.getGrupoParoquialId());

        ocupacao.setStatus(AtivoAlocacaoStatus.DEVOLVIDA);
        ocupacao.setDevolvidoEmUtc(Instant.now());
        alocacaoRepository.save(ocupacao);

        ativo.setStatus(AtivoStatus.DISPONIVEL);
        AtivoPatrimonialEntity saved = ativoRepository.save(ativo);

        String observacao = request != null && request.observacao() != null
                ? request.observacao() : "Ativo devolvido";
        createNotaUseCase.execute(saved.getId(), actorContext.usuarioId(), TipoNotaAtivoPatrimonial.DEVOLUCAO,
                observacao);
        auditPublisher.publishDesalocacao(actorContext.actor(), saved.getId(), saved.getGrupoParoquialId());

        return CreateAtivoPatrimonialUseCase.toResponse(saved);
    }
}
