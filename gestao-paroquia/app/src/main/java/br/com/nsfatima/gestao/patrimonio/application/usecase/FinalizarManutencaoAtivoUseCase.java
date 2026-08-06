package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNotFoundException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoAlocacaoTipo;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
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
 * Encerra o período de manutenção corrente de um ativo patrimonial, marcando a ocupação de
 * manutenção como {@code DEVOLVIDA} e retornando o ativo ao status {@code DISPONIVEL}.
 */
@Service
public class FinalizarManutencaoAtivoUseCase {

    private static final List<AtivoAlocacaoStatus> STATUS_FINALIZAVEIS = List.of(
            AtivoAlocacaoStatus.CONFIRMADA, AtivoAlocacaoStatus.ATRASADA);

    private final AtivoPatrimonialJpaRepository ativoRepository;
    private final AtivoAlocacaoJpaRepository alocacaoRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;

    public FinalizarManutencaoAtivoUseCase(AtivoPatrimonialJpaRepository ativoRepository,
                                            AtivoAlocacaoJpaRepository alocacaoRepository,
                                            EventoActorContextResolver actorContextResolver,
                                            PatrimonioAuthorizationService authorizationService) {
        this.ativoRepository = ativoRepository;
        this.alocacaoRepository = alocacaoRepository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
    }

    /**
     * Finaliza a manutenção corrente do ativo, localizando a ocupação de manutenção em aberto
     * (status {@code CONFIRMADA} ou {@code ATRASADA}) e devolvendo o ativo ao inventário
     * disponível.
     *
     * @param ativoId ativo em manutenção
     * @return o ativo já com status {@code DISPONIVEL}
     * @throws AtivoPatrimonialNotFoundException se o ativo não existir
     * @throws AtivoManutencaoInvalidaException  se não houver manutenção corrente para finalizar
     */
    @Transactional
    public AtivoPatrimonialResponse execute(UUID ativoId) {
        AtivoPatrimonialEntity ativo = ativoRepository.findById(ativoId)
                .orElseThrow(() -> new AtivoPatrimonialNotFoundException(
                        "Ativo patrimonial não encontrado: " + ativoId));

        AtivoAlocacaoEntity manutencao = alocacaoRepository.findByAtivoIdAndStatusIn(ativoId, STATUS_FINALIZAVEIS)
                .stream()
                .filter(ocupacao -> ocupacao.getTipo() == AtivoAlocacaoTipo.MANUTENCAO)
                .findFirst()
                .orElseThrow(() -> new AtivoManutencaoInvalidaException(
                        "O ativo não possui uma manutenção corrente para finalizar"));

        EventoActorContext actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanIniciarManutencao(actorContext, ativo.getGrupoParoquialId());

        manutencao.setStatus(AtivoAlocacaoStatus.DEVOLVIDA);
        manutencao.setDevolvidoEmUtc(Instant.now());
        alocacaoRepository.save(manutencao);

        ativo.setStatus(AtivoStatus.DISPONIVEL);
        AtivoPatrimonialEntity saved = ativoRepository.save(ativo);

        return CreateAtivoPatrimonialUseCase.toResponse(saved);
    }
}
