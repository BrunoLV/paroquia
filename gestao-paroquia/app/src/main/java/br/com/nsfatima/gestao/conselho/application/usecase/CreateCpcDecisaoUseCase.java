package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CreateCpcDecisaoRequest;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.CpcDecisaoEntity;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository.CpcDecisaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Registra uma nova ata/decisão de conselho paroquial (CPC/CPP) a partir de uma reunião.
 * Toda decisão nasce em {@link StatusHomologacao#PENDENTE_HOMOLOGACAO}: o registro do fato
 * (quem propôs, quando, sobre o que) é imediato, mas só passa a ter efeito oficial após
 * homologação por uma autoridade competente ({@link HomologarCpcDecisaoUseCase}) — a criação
 * em si não representa aprovação da decisão.
 */
@Service
public class CreateCpcDecisaoUseCase {

    private final CpcDecisaoJpaRepository repository;
    private final EventoActorContextResolver actorContextResolver;

    public CreateCpcDecisaoUseCase(
            CpcDecisaoJpaRepository repository, EventoActorContextResolver actorContextResolver) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
    }

    /**
     * Cria a ata/decisão com status inicial {@link StatusHomologacao#PENDENTE_HOMOLOGACAO},
     * registrando o ator autenticado como solicitante (id e papel/role, não o nome pessoal —
     * ver invariante de LGPD do contexto). O tipo de conselho é fixado como "CPC" nesta
     * versão do fluxo.
     *
     * @param request dados da pauta/ata (título, descrição, data da reunião e comunidade)
     * @return a decisão recém-criada, já com status pendente de homologação
     */
    @Transactional
    public CpcDecisaoResponse execute(CreateCpcDecisaoRequest request) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        CpcDecisaoEntity entity = new CpcDecisaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTitulo(request.titulo());
        entity.setPautaDescricao(request.pautaDescricao());
        entity.setDataReuniao(request.dataReuniao());
        entity.setComunidadeId(request.comunidadeId());
        entity.setConselhoTipo("CPC");
        entity.setStatusHomologacao(StatusHomologacao.PENDENTE_HOMOLOGACAO);
        entity.setSolicitanteId(context.actor());
        entity.setSolicitanteNome(context.role());
        entity.setCriadoEmUtc(Instant.now());

        CpcDecisaoEntity saved = repository.save(entity);
        return toResponse(saved);
    }

    /**
     * Converte a entidade persistida em DTO de resposta da API. Reutilizado pelos demais
     * use cases do contexto ({@link HomologarCpcDecisaoUseCase}, {@link ListCpcDecisoesUseCase})
     * para manter um único mapeamento canônico entidade → resposta.
     *
     * @param e entidade de decisão de conselho já persistida
     * @return DTO de resposta com os dados públicos da decisão
     */
    public static CpcDecisaoResponse toResponse(CpcDecisaoEntity e) {
        return new CpcDecisaoResponse(
                e.getId(),
                e.getTitulo(),
                e.getPautaDescricao(),
                e.getDataReuniao(),
                e.getComunidadeId(),
                e.getConselhoTipo(),
                e.getStatusHomologacao(),
                e.getSolicitanteId(),
                e.getSolicitanteNome(),
                e.getHomologadorId(),
                e.getMotivoVeto(),
                e.getCriadoEmUtc(),
                e.getDecididoEmUtc()
        );
    }
}
