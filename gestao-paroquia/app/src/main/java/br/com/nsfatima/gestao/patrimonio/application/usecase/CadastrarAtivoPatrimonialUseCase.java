package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CadastrarAtivoPatrimonialRequest;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.CodigoTombamento;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.entity.UnidadeTerritorialEntity;
import br.com.nsfatima.gestao.organizacao.infrastructure.persistence.repository.UnidadeTerritorialJpaRepository;
import br.com.nsfatima.gestao.patrimonio.domain.exception.AtivoGrupoParoquialInvalidaException;
import br.com.nsfatima.gestao.patrimonio.domain.policy.PropriedadeUnidadeTerritorialPolicy;
import br.com.nsfatima.gestao.patrimonio.application.service.NumeroTombamentoGenerator;
import br.com.nsfatima.gestao.patrimonio.application.service.PatrimonioAuthorizationService;
import br.com.nsfatima.gestao.patrimonio.infrastructure.observability.PatrimonioAuditPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.Year;

/**
 * Cadastra um novo ativo patrimonial vinculado de forma fixa a uma unidade territorial
 * proprietária (MATRIZ ou COMUNIDADE), no status inicial {@code DISPONIVEL} e com o código de
 * tombamento anual gerado automaticamente.
 */
@Service
public class CadastrarAtivoPatrimonialUseCase {

    private final AtivoPatrimonialRepository repository;
    private final EventoActorContextResolver actorContextResolver;
    private final PatrimonioAuthorizationService authorizationService;
    private final UnidadeTerritorialJpaRepository unidadeTerritorialRepository;
    private final PropriedadeUnidadeTerritorialPolicy propriedadeUnidadeTerritorialPolicy;
    private final NumeroTombamentoGenerator numeroTombamentoGenerator;
    private final PatrimonioAuditPublisher auditPublisher;

    public CadastrarAtivoPatrimonialUseCase(AtivoPatrimonialRepository repository,
                                            EventoActorContextResolver actorContextResolver,
                                            PatrimonioAuthorizationService authorizationService,
                                            UnidadeTerritorialJpaRepository unidadeTerritorialRepository,
                                            PropriedadeUnidadeTerritorialPolicy propriedadeUnidadeTerritorialPolicy,
                                            NumeroTombamentoGenerator numeroTombamentoGenerator,
                                            PatrimonioAuditPublisher auditPublisher) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.unidadeTerritorialRepository = unidadeTerritorialRepository;
        this.propriedadeUnidadeTerritorialPolicy = propriedadeUnidadeTerritorialPolicy;
        this.numeroTombamentoGenerator = numeroTombamentoGenerator;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    public AtivoPatrimonialResponse execute(CadastrarAtivoPatrimonialRequest request) {
        EventoActorContext actor = actorContextResolver.resolveRequired();
        authorizationService.assertPodeGerirPatrimonio(actor, request.grupoParoquialId());
        UnidadeTerritorialEntity proprietaria = unidadeTerritorialRepository.findById(request.grupoParoquialId())
                .orElseThrow(() -> new AtivoGrupoParoquialInvalidaException(
                        "Unidade territorial não encontrada: " + request.grupoParoquialId()));
        propriedadeUnidadeTerritorialPolicy.assertPermitida(proprietaria.getTipo());

        Instant agora = Instant.now();
        CodigoTombamento codigo = new CodigoTombamento(
                numeroTombamentoGenerator.generate(Year.now().getValue()));

        AtivoPatrimonial ativo = AtivoPatrimonial.cadastrar(
                AtivoPatrimonialId.generate(),
                request.nome(),
                codigo,
                request.descricao(),
                request.categoria(),
                request.dataAquisicao(),
                request.valorAquisicao(),
                request.grupoParoquialId(),
                request.isReservavel(),
                actor.usuarioId(),
                agora);

        AtivoPatrimonial salvo = repository.save(ativo);
        auditPublisher.publishCreateSuccess(actor.actor(), salvo.getId().value(), salvo.getGrupoParoquialId());
        return AtivoPatrimonialResponse.de(salvo);
    }
}
