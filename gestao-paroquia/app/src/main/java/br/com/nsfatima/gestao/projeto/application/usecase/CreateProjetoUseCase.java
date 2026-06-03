package br.com.nsfatima.gestao.projeto.application.usecase;

import br.com.nsfatima.gestao.projeto.application.dto.ProjetoCreationResult;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoCreateRequest;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoAprovacaoResponse;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.domain.service.ProjetoAuthorizationService;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper.ProjetoMapper;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoEventoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.repository.ProjetoSolicitacaoAprovacaoJpaRepository;
import br.com.nsfatima.gestao.projeto.infrastructure.observability.ProjetoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.repository.AnoParoquialJpaRepository;
import tools.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.time.ZoneOffset;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateProjetoUseCase {

    private final ProjetoEventoJpaRepository repository;
    private final ProjetoMapper mapper;
    private final ProjetoAuditPublisher auditPublisher;
    private final EventoActorContextResolver actorContextResolver;
    private final ProjetoAuthorizationService authorizationService;
    private final AnoParoquialJpaRepository anoParoquialRepository;
    private final ProjetoSolicitacaoAprovacaoJpaRepository approvalRepository;
    private final ObjectMapper objectMapper;

    public CreateProjetoUseCase(
            ProjetoEventoJpaRepository repository,
            ProjetoMapper mapper,
            ProjetoAuditPublisher auditPublisher,
            EventoActorContextResolver actorContextResolver,
            ProjetoAuthorizationService authorizationService,
            AnoParoquialJpaRepository anoParoquialRepository,
            ProjetoSolicitacaoAprovacaoJpaRepository approvalRepository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditPublisher = auditPublisher;
        this.actorContextResolver = actorContextResolver;
        this.authorizationService = authorizationService;
        this.anoParoquialRepository = anoParoquialRepository;
        this.approvalRepository = approvalRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Coordena a criação de um novo projeto, garantindo que as regras de negócio sejam aplicadas e a ação seja auditada.
     * Se o ano paroquial correspondente estiver fechado ou concluído, a criação é encaminhada para aprovação.
     *
     * @param request Dados para criação do projeto
     * @return Resultado contendo ou o projeto criado ou a solicitação de aprovação pendente
     */
    @Transactional
    public ProjetoCreationResult create(ProjetoCreateRequest request) {
        var actorContext = actorContextResolver.resolveRequired();
        authorizationService.assertCanCreate(actorContext, request.organizacaoResponsavelId());

        if (request.fim().isBefore(request.inicio())) {
            throw new IllegalArgumentException("Project end date cannot be before start date");
        }

        int startYear = request.inicio().atZone(ZoneOffset.UTC).getYear();
        var anoParoquialOpt = anoParoquialRepository.findByAno(startYear);

        boolean isLocked = false;
        if (anoParoquialOpt.isPresent()) {
            String status = anoParoquialOpt.get().getStatus();
            if ("FECHADO".equals(status) || "CONCLUIDO".equals(status)) {
                isLocked = true;
            }
        }

        if (isLocked) {
            ProjetoSolicitacaoAprovacaoEntity approval = new ProjetoSolicitacaoAprovacaoEntity();
            approval.setId(UUID.randomUUID());
            approval.setProjetoId(null);
            approval.setTipoSolicitacao("CRIACAO");
            approval.setStatus("PENDENTE");
            approval.setSolicitanteId(actorContext.actor());
            approval.setSolicitantePapel(actorContext.role());
            approval.setSolicitanteTipoOrganizacao(actorContext.organizationType());
            try {
                approval.setActionPayloadJson(objectMapper.writeValueAsString(request));
            } catch (Exception e) {
                throw new RuntimeException("Error serializing project request", e);
            }
            approval.setCancelarEventosVinculados(false);
            approval.setCriadoEmUtc(Instant.now());

            ProjetoSolicitacaoAprovacaoEntity savedApproval = approvalRepository.save(approval);
            auditPublisher.publish(actorContext.actor(), "request-create-project", savedApproval.getId().toString(), "success");

            return new ProjetoCreationResult(null, mapper.toAprovacaoResponse(savedApproval));
        }

        ProjetoEventoEntity entity = new ProjetoEventoEntity();
        entity.setId(UUID.randomUUID());
        entity.setNome(request.nome());
        entity.setDescricao(request.descricao());
        entity.setOrganizacaoResponsavelId(request.organizacaoResponsavelId());
        entity.setInicioUtc(request.inicio());
        entity.setFimUtc(request.fim());
        entity.setStatus(ProjetoStatus.ABERTO.name());
        
        ProjetoEventoEntity saved = repository.save(entity);
        
        auditPublisher.publishCreateSuccess(actorContext.actor(), saved.getId().toString());
        
        return new ProjetoCreationResult(mapper.toResponse(saved), null);
    }
}

