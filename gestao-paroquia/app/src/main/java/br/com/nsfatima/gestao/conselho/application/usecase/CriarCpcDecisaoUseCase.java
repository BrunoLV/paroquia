package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CriarCpcDecisaoRequest;
import br.com.nsfatima.gestao.conselho.domain.model.CpcDecisao;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.domain.repository.CpcDecisaoRepository;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.mapper.CpcDecisaoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Registra uma nova ata/decisão de conselho paroquial (CPC/CPP) a partir de uma reunião.
 * Toda decisão nasce em {@link StatusHomologacao#PENDENTE_HOMOLOGACAO}: o registro do fato
 * (quem propôs, quando, sobre o que) é imediato, mas só passa a ter efeito oficial após
 * homologação por uma autoridade competente ({@link HomologarCpcDecisaoUseCase}).
 */
@Service
public class CriarCpcDecisaoUseCase {

    private final CpcDecisaoRepository repository;
    private final EventoActorContextResolver actorContextResolver;
    private final CpcDecisaoMapper mapper;

    public CriarCpcDecisaoUseCase(
            CpcDecisaoRepository repository,
            EventoActorContextResolver actorContextResolver,
            CpcDecisaoMapper mapper) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
        this.mapper = mapper;
    }

    /**
     * Cria a ata/decisão com status inicial {@link StatusHomologacao#PENDENTE_HOMOLOGACAO},
     * registrando o ator autenticado como solicitante (id e papel/role, nunca o nome pessoal — LGPD).
     *
     * @param request dados da pauta/ata (título, descrição, data da reunião e comunidade)
     * @return a decisão recém-criada, já com status pendente de homologação
     */
    @Transactional
    public CpcDecisaoResponse execute(CriarCpcDecisaoRequest request) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        CpcDecisao decisao = CpcDecisao.registrar(
                UUID.randomUUID(),
                request.titulo(),
                request.pautaDescricao(),
                request.dataReuniao(),
                request.comunidadeId(),
                context.actor(),
                context.role(),
                Instant.now());

        return mapper.toResponse(repository.salvar(decisao));
    }
}
