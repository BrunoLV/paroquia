package br.com.nsfatima.gestao.patrimonio.application.usecase;

import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.AtivoPatrimonialResponse;
import br.com.nsfatima.gestao.patrimonio.api.v1.dto.CadastrarAtivoPatrimonialRequest;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.CodigoTombamento;
import br.com.nsfatima.gestao.patrimonio.domain.repository.AtivoPatrimonialRepository;
import br.com.nsfatima.gestao.patrimonio.domain.service.AtivoGrupoParoquialPolicy;
import br.com.nsfatima.gestao.patrimonio.domain.service.NumeroTombamentoGenerator;
import br.com.nsfatima.gestao.patrimonio.domain.service.PatrimonioAuthorizationService;
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
    private final AtivoGrupoParoquialPolicy grupoParoquialPolicy;
    private final NumeroTombamentoGenerator numeroTombamentoGenerator;
    private final PatrimonioAuditPublisher auditPublisher;

    public CadastrarAtivoPatrimonialUseCase(AtivoPatrimonialRepository repository,
                                            EventoActorContextResolver actorContextResolver,
                                            PatrimonioAuthorizationService authorizationService,
                                            AtivoGrupoParoquialPolicy grupoParoquialPolicy,
                                            NumeroTombamentoGenerator numeroTombamentoGenerator,
                                            PatrimonioAuditPublisher auditPublisher) {
        this.repository = repository;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.grupoParoquialPolicy = grupoParoquialPolicy;
        this.numeroTombamentoGenerator = numeroTombamentoGenerator;
        this.auditPublisher = auditPublisher;
    }

    @Transactional
    public AtivoPatrimonialResponse execute(CadastrarAtivoPatrimonialRequest request) {
        EventoActorContext actor = actorContextResolver.resolveRequired();
        authorizationService.assertPodeGerirPatrimonio(actor, request.grupoParoquialId());
        grupoParoquialPolicy.assertGrupoParoquialProprietariaPermitida(request.grupoParoquialId());

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
