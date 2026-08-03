package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao.strategy;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoDecisionResponse;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.application.usecase.strategy.EstrategiaExecucaoAprovacao;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoResponse;
import br.com.nsfatima.gestao.calendario.application.usecase.evento.CreateEventoUseCase;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import org.springframework.stereotype.Component;

@Component
public class CreateEventoExecutionStrategy implements EstrategiaExecucaoAprovacao {

    private final CreateEventoUseCase createEventoUseCase;
    private final EventoJpaRepository eventoRepository;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;

    public CreateEventoExecutionStrategy(
            CreateEventoUseCase createEventoUseCase,
            EventoJpaRepository eventoRepository,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper) {
        this.createEventoUseCase = createEventoUseCase;
        this.eventoRepository = eventoRepository;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
    }

    @Override
    public boolean supports(TipoSolicitacaoInput tipo) {
        return tipo == TipoSolicitacaoInput.CRIACAO_EVENTO;
    }

    @Override
    public AprovacaoDecisionResponse.ActionExecution execute(AprovacaoEntity aprovacao,
                                                             EventoActorContext actorContext) {

        AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(aprovacao.getActionPayloadJson());
        String idempotencyKey = payload.idempotencyKey() == null
                ? "aprovacao-exec-" + aprovacao.getId()
                : payload.idempotencyKey();

        EventoResponse response = createEventoUseCase.executeApprovedCreation(
                createEventoUseCase.restoreFromAprovacaoPayload(payload),
                idempotencyKey);

        aprovacao.setEventoId(response.id());

        return new AprovacaoDecisionResponse.ActionExecution(
                "EXECUTED",
                response.id(),
                response.status() == null ? null : response.status().name(),
                null);
    }

    @Override
    public String fetchResourceStatus(AprovacaoEntity aprovacao) {
        if (aprovacao.getEventoId() != null) {
            return eventoRepository.findStatusByIdNoLock(aprovacao.getEventoId()).orElse(null);
        }
        return null;
    }
}
