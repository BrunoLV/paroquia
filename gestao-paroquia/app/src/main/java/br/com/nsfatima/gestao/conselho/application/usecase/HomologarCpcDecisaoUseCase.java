package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.conselho.domain.exception.CpcDecisaoNotFoundException;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.HomologarCpcDecisaoRequest;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.CpcDecisaoEntity;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository.CpcDecisaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Homologa ou veta uma ata/decisão de conselho previamente registrada, encerrando o fluxo
 * iniciado por {@link CreateCpcDecisaoUseCase}. Esta é a única forma de uma decisão sair de
 * {@link StatusHomologacao#PENDENTE_HOMOLOGACAO} e produzir efeito oficial — reservada, na
 * camada de API, a Pároco, Admin ou Mesa CPP (a validação de papel/role em si é feita no
 * controller via {@code @PreAuthorize}, não aqui).
 */
@Service
public class HomologarCpcDecisaoUseCase {

    private final CpcDecisaoJpaRepository repository;
    private final EventoActorContextResolver actorContextResolver;

    public HomologarCpcDecisaoUseCase(
            CpcDecisaoJpaRepository repository, EventoActorContextResolver actorContextResolver) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Aplica a decisão de homologação: registra o homologador e a data/hora da decisão e,
     * quando o status é {@link StatusHomologacao#VETADO}, exige e grava o motivo do veto —
     * invariante de negócio que impede vetos sem justificativa. Em caso de homologação
     * (não-veto) qualquer motivo de veto anterior é limpo.
     *
     * @param id identificador da decisão a homologar/vetar
     * @param request status decidido (homologado/vetado) e, se aplicável, o motivo do veto
     * @return a decisão atualizada, já com o resultado da homologação
     * @throws CpcDecisaoNotFoundException se não existir decisão com o {@code id} informado
     * @throws IllegalArgumentException se o status for {@link StatusHomologacao#VETADO} sem motivo de veto informado
     */
    @Transactional
    public CpcDecisaoResponse execute(UUID id, HomologarCpcDecisaoRequest request) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        CpcDecisaoEntity entity = repository.findById(id)
                .orElseThrow(() -> new CpcDecisaoNotFoundException(id));

        if (request.status() == StatusHomologacao.VETADO
                && (request.motivoVeto() == null || request.motivoVeto().isBlank())) {
            throw new IllegalArgumentException("Motivo do veto é obrigatório ao vetar uma decisão");
        }

        entity.setStatusHomologacao(request.status());
        entity.setHomologadorId(context.actor());
        entity.setMotivoVeto(request.status() == StatusHomologacao.VETADO ? request.motivoVeto() : null);
        entity.setDecididoEmUtc(Instant.now());

        CpcDecisaoEntity saved = repository.save(entity);
        return CreateCpcDecisaoUseCase.toResponse(saved);
    }
}
