package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.domain.event.ManutencaoAtivoConcluidaEvent;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoManutencaoInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoPatrimonialNaoEncontradoException;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencao;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoManutencaoRepository;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.patrimonio.application.service.PatrimonioAuthorizationService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Conclui a manutenção em aberto de um ativo: registra {@code dataFimReal} e devolve o bem ao
 * status {@code DISPONIVEL}. O evento publicado dispara o recálculo de {@code RESERVADO} (o bem
 * pode ter uma reserva ativa aguardando).
 */
@Service
public class ConcluirManutencaoAtivoUseCase {

    private final AtivoPatrimonialRepository ativoRepository;
    private final AtivoManutencaoRepository manutencaoRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final ApplicationEventPublisher eventPublisher;

    public ConcluirManutencaoAtivoUseCase(AtivoPatrimonialRepository ativoRepository,
                                          AtivoManutencaoRepository manutencaoRepository,
                                          EventoActorContextResolver actorContextResolver,
                                          PatrimonioAuthorizationService authorizationService,
                                          ApplicationEventPublisher eventPublisher) {
        this.ativoRepository = ativoRepository;
        this.manutencaoRepository = manutencaoRepository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public AtivoPatrimonialResponse execute(UUID ativoId) {
        AtivoPatrimonial ativo = ativoRepository.findById(new AtivoPatrimonialId(ativoId))
                .filter(a -> !a.estaExcluido())
                .orElseThrow(() -> new AtivoPatrimonialNaoEncontradoException("Ativo não encontrado: " + ativoId));

        EventoActorContext actor = actorContextResolver.resolveRequired();
        authorizationService.assertPodeGerirPatrimonio(actor, ativo.getGrupoParoquialId());

        AtivoManutencao manutencao = manutencaoRepository.findEmAbertoPorAtivo(ativo.getId())
                .orElseThrow(() -> new AtivoManutencaoInvalidaException(
                        "Não há manutenção em aberto para concluir neste ativo."));

        Instant agora = Instant.now();
        manutencao.concluir(agora);
        manutencaoRepository.save(manutencao);

        ativo.concluirManutencao(agora);
        AtivoPatrimonial salvo = ativoRepository.save(ativo);

        eventPublisher.publishEvent(new ManutencaoAtivoConcluidaEvent(ativoId));
        return AtivoPatrimonialResponse.de(salvo);
    }
}
