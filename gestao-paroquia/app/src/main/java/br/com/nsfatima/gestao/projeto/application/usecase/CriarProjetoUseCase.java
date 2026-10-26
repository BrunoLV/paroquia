package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquialStatus;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import br.com.nsfatima.gestao.projeto.api.v1.dto.CriarProjetoRequest;
import br.com.nsfatima.gestao.projeto.application.dto.ResultadoCriacaoProjeto;
import br.com.nsfatima.gestao.projeto.application.service.ProjetoAuthorizationService;
import br.com.nsfatima.gestao.projeto.domain.model.Projeto;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.observability.ProjetoAuditPublisher;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Orquestra o cadastro de novos projetos pastorais ({@link ProjetoEventoEntity}).
 *
 * <p>Regra de bloqueio por ano paroquial:
 * <ul>
 *   <li>Se o ano de início do projeto pertencer a um {@link br.com.nsfatima.gestao.governanca.domain.model.AnoParoquial}
 *       com status {@code FECHADO} ou {@code CONCLUIDO}, a criação direta é bloqueada;</li>
 *   <li>Em vez de rejeitar a operação, é gerada uma solicitação de aprovação pendente
 *       ({@link ProjetoSolicitacaoAprovacaoEntity} do tipo {@code CRIACAO}) contendo o payload
 *       serializado, retornada em {@link ResultadoCriacaoProjeto#aprovacao()};</li>
 *   <li>Se o ano estiver aberto, o projeto é persistido imediatamente no status
 *       {@link ProjetoStatus#ABERTO}, retornado em {@link ResultadoCriacaoProjeto#projeto()}.</li>
 * </ul>
 */
@Service
public class CriarProjetoUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoMapper mapper;
    private final ProjetoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;
    private final ProjetoAuthorizationService authorizationService;
    private final AnoParoquialJpaRepository anoParoquialRepository;
    private final ProjetoSolicitacaoAprovacaoJpaRepository aprovacaoRepository;
    private final ObjectMapper objectMapper;

    public CriarProjetoUseCase(
            ProjetoEventoJpaRepository repository,
            ProjetoMapper mapper,
            ProjetoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver,
            ProjetoAuthorizationService authorizationService,
            AnoParoquialJpaRepository anoParoquialRepository,
            ProjetoSolicitacaoAprovacaoJpaRepository aprovacaoRepository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.anoParoquialRepository = anoParoquialRepository;
        this.aprovacaoRepository = aprovacaoRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Coordena a criação de um novo projeto, garantindo que as regras de negócio sejam aplicadas e a ação seja
     * auditada.
     * Se o ano paroquial correspondente estiver fechado ou concluído, a criação é encaminhada para aprovação.
     *
     * @param request Dados para criação do projeto
     * @return Resultado contendo ou o projeto criado ou a solicitação de aprovação pendente
     */
    @Transactional
    public ResultadoCriacaoProjeto criar(CriarProjetoRequest request) {
        var actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanCreate(actorContext, request.organizacaoResponsavelId());

        if (request.fim().isBefore(request.inicio())) {
            throw new IllegalArgumentException("Data de fim do projeto não pode ser anterior à data de início");
        }

        int startYear = request.inicio().atZone(ZoneOffset.UTC).getYear();
        var anoParoquialOpt = anoParoquialRepository.findByAno(startYear);

        boolean isLocked = false;
        if (anoParoquialOpt.isPresent()) {
            AnoParoquialStatus status = anoParoquialOpt.get().getStatus();
            if (status == AnoParoquialStatus.FECHADO || status == AnoParoquialStatus.CONCLUIDO) {
                isLocked = true;
            }
        }

        if (isLocked) {
            ProjetoSolicitacaoAprovacaoEntity aprovacao = new ProjetoSolicitacaoAprovacaoEntity();
            aprovacao.setId(UUID.randomUUID());
            aprovacao.setProjetoId(null);
            aprovacao.setTipoSolicitacao("CRIACAO");
            aprovacao.setStatus(AprovacaoStatus.PENDENTE);
            aprovacao.setSolicitanteId(actorContext.actor());
            aprovacao.setSolicitantePapel(actorContext.role());
            aprovacao.setSolicitanteTipoOrganizacao(TipoOrganizacao.fromStoredValue(actorContext.organizationType()));
            try {
                aprovacao.setActionPayloadJson(objectMapper.writeValueAsString(request));
            } catch (Exception e) {
                throw new RuntimeException("Error serializing project request", e);
            }
            aprovacao.setCancelarEventosVinculados(false);

            ProjetoSolicitacaoAprovacaoEntity savedAprovacao = aprovacaoRepository.save(aprovacao);
            auditPublisher.publish(actorContext.actor(),
                    "request-create-project", savedAprovacao.getId().toString(), "success");

            return new ResultadoCriacaoProjeto(null, mapper.toAprovacaoResponse(savedAprovacao));
        }

        Projeto projeto = Projeto.abrir(
                UUID.randomUUID(),
                request.nome(),
                request.descricao(),
                request.organizacaoResponsavelId(),
                request.inicio(),
                request.fim());

        ProjetoEventoEntity saved = repository.save(mapper.toEntity(projeto));

        auditPublisher.publishCreateSuccess(actorContext.actor(), saved.getId().toString());

        return new ResultadoCriacaoProjeto(mapper.toResponse(saved), null);
    }
}
