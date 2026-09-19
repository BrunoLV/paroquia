package br.com.nsfatima.gestao.conselho.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.conselho.api.v1.dto.CpcDecisaoResponse;
import br.com.nsfatima.gestao.conselho.api.v1.dto.HomologarCpcDecisaoRequest;
import br.com.nsfatima.gestao.conselho.domain.exception.CpcDecisaoNaoEncontradaException;
import br.com.nsfatima.gestao.conselho.domain.model.CpcDecisao;
import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.domain.repository.CpcDecisaoRepository;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.mapper.CpcDecisaoMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Homologa ou veta uma ata/decisão de conselho previamente registrada, encerrando o fluxo
 * iniciado por {@link CriarCpcDecisaoUseCase}. Toda a regra de transição vive no agregado
 * {@link CpcDecisao}: só uma decisão {@link StatusHomologacao#PENDENTE_HOMOLOGACAO} pode ser
 * decidida, e vetar exige motivo.
 */
@Service
public class HomologarCpcDecisaoUseCase {

    private final CpcDecisaoRepository repository;
    private final EventoActorContextResolver actorContextResolver;
    private final CpcDecisaoMapper mapper;

    public HomologarCpcDecisaoUseCase(
            CpcDecisaoRepository repository,
            EventoActorContextResolver actorContextResolver,
            CpcDecisaoMapper mapper) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
        this.mapper = mapper;
    }

    /**
     * Carrega a decisão, dispara o comando de negócio ({@code homologar}/{@code vetar}) no
     * agregado e persiste o resultado.
     *
     * @param id identificador da decisão a homologar/vetar
     * @param request status decidido (homologado/vetado) e, se aplicável, o motivo do veto
     * @return a decisão atualizada
     * @throws CpcDecisaoNaoEncontradaException se não existir decisão com o {@code id} informado
     * @throws IllegalArgumentException se a decisão não estiver pendente, ou se vetar sem motivo
     */
    @Transactional
    public CpcDecisaoResponse execute(UUID id, HomologarCpcDecisaoRequest request) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        CpcDecisao decisao = repository.buscarPorId(id)
                .orElseThrow(() -> new CpcDecisaoNaoEncontradaException(id));

        if (request.status() == StatusHomologacao.VETADO) {
            decisao.vetar(context.actor(), request.motivoVeto(), Instant.now());
        } else {
            decisao.homologar(context.actor(), Instant.now());
        }

        return mapper.toResponse(repository.salvar(decisao));
    }
}
