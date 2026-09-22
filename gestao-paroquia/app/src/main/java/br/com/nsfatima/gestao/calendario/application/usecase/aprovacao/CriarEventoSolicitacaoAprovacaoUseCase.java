package br.com.nsfatima.gestao.calendario.application.usecase.aprovacao;

import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.CriarEventoRequest;
import br.com.nsfatima.gestao.calendario.api.v1.dto.evento.EventoAprovacaoPendenteResponse;
import br.com.nsfatima.gestao.calendario.domain.type.AprovadorPapel;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoInput;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.EventoAuditPublisher;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContext;
import br.com.nsfatima.gestao.calendario.infrastructure.security.EventoActorContextResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.nsfatima.gestao.calendario.application.dto.ConflitoEventoItemDto;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Abre solicitação formal de aprovação para evento submetido por coordenação pastoral.
 */
@Service
public class CriarEventoSolicitacaoAprovacaoUseCase {

    private final AprovacaoJpaRepository aprovacaoRepository;
    private final EventoActorContextResolver actorContextResolver;
    private final EventoAuditPublisher auditPublisher;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;
    private final ObjectMapper objectMapper;

    public CriarEventoSolicitacaoAprovacaoUseCase(
            AprovacaoJpaRepository aprovacaoRepository,
            EventoActorContextResolver actorContextResolver,
            EventoAuditPublisher auditPublisher,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper,
            ObjectMapper objectMapper) {
        this.aprovacaoRepository = aprovacaoRepository;
        this.actorContextResolver = actorContextResolver;
        this.auditPublisher = auditPublisher;
        this.aprovacaoAcaoPayloadMapper = aprovacaoAcaoPayloadMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * Registra uma solicitação formal de criação de evento sem lista pré-computada de conflitos.
     */
    @Transactional
    public EventoAprovacaoPendenteResponse execute(String idempotencyKey, CriarEventoRequest request) {
        return execute(idempotencyKey, request, null, request.adicionadoExtraJustificativa());
    }

    /**
     * Registra uma solicitação formal de criação de evento com detalhes analíticos dos conflitos
     * identificados e justificativa mandatória/opcional.
     */
    @Transactional
    public EventoAprovacaoPendenteResponse execute(
            String idempotencyKey,
            CriarEventoRequest request,
            List<ConflitoEventoItemDto> conflitos,
            String justificativa) {
        EventoActorContext context = actorContextResolver.resolveRequired();

        AprovacaoEntity entity = new AprovacaoEntity();
        entity.setId(UUID.randomUUID());
        entity.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
        entity.setStatus(AprovacaoStatus.PENDENTE);
        entity.setSolicitanteId(context.actor());
        entity.setSolicitantePapel(context.role());
        entity.setSolicitanteTipoGrupo(TipoGrupo.fromStoredValue(context.organizationType()));
        entity.setAprovadorPapel(AprovadorPapel.COORD_CONSELHO.storedValue());
        entity.setCriadoEmUtc(Instant.now());
        entity.setDataOcorrenciaUtc(request.inicio());
        entity.setJustificativa(justificativa != null ? justificativa : request.adicionadoExtraJustificativa());

        if (conflitos != null && !conflitos.isEmpty()) {
            try {
                entity.setConflitosJson(objectMapper.writeValueAsString(conflitos));
            } catch (JacksonException ex) {
                entity.setConflitosJson("[]");
            }
        }

        AprovacaoAcaoPayload payload = buildPayload(idempotencyKey, request);
        entity.setActionPayloadJson(aprovacaoAcaoPayloadMapper.toJson(payload));

        aprovacaoRepository.save(entity);

        if (request.regraRecorrencia() != null) {
            gerarOcorrenciasRecorrentes(entity, request, idempotencyKey, context);
        }

        auditPublisher.publish(context.actor(), "aprovacao-decisao-solicitacao", entity.getId().toString(), "sucesso");

        return new EventoAprovacaoPendenteResponse(
                entity.getId(),
                entity.getStatus().name(),
                "APROVACAO_PENDENTE");
    }

    private void gerarOcorrenciasRecorrentes(
            AprovacaoEntity mestre,
            CriarEventoRequest request,
            String idempotencyKey,
            EventoActorContext context) {
        LocalDate startOfSeries = request.inicio().atZone(ZoneOffset.UTC).toLocalDate();
        int year = startOfSeries.getYear();
        LocalDate endOfYear = LocalDate.of(year, 12, 31);
        List<LocalDate> dates = request.regraRecorrencia().gerarDatas(startOfSeries, endOfYear);

        Duration duration = Duration.between(request.inicio(), request.fim());
        Duration timeOfDay = Duration.between(
                startOfSeries.atStartOfDay().toInstant(ZoneOffset.UTC), request.inicio());

        for (LocalDate date : dates) {
            Instant occStart;
            Instant occEnd;
            if (request.tipoHorario() != null && request.tipoHorario().isPredefinido()) {
                var faixa = request.tipoHorario().calcularIntervalo(date, ZoneId.of("America/Sao_Paulo"));
                occStart = faixa.inicioUtc();
                occEnd = faixa.fimUtc();
            } else {
                occStart = date.atStartOfDay().toInstant(ZoneOffset.UTC).plus(timeOfDay);
                occEnd = occStart.plus(duration);
            }

            AprovacaoEntity filha = new AprovacaoEntity();
            filha.setId(UUID.randomUUID());
            filha.setAprovacaoPaiId(mestre.getId());
            filha.setTipoSolicitacao(TipoSolicitacaoInput.CRIACAO_EVENTO.name());
            filha.setStatus(AprovacaoStatus.PENDENTE);
            filha.setSolicitanteId(context.actor());
            filha.setSolicitantePapel(context.role());
            filha.setSolicitanteTipoGrupo(TipoGrupo.fromStoredValue(context.organizationType()));
            filha.setAprovadorPapel(AprovadorPapel.COORD_CONSELHO.storedValue());
            filha.setCriadoEmUtc(Instant.now());
            filha.setDataOcorrenciaUtc(occStart);
            filha.setJustificativa(mestre.getJustificativa());

            CriarEventoRequest occRequest = new CriarEventoRequest(
                    request.titulo(),
                    request.descricao(),
                    request.categoria(),
                    request.grupoParoquialResponsavelId(),
                    request.projetoId(),
                    request.localId(),
                    request.localIds(),
                    request.igrejaVinculoId(),
                    occStart,
                    occEnd,
                    request.status(),
                    request.adicionadoExtraJustificativa(),
                    request.participantes(),
                    request.liberarConflitoAgenda(),
                    request.ativoIds(),
                    null,
                    request.confirmarComConflito(),
                    request.tipoHorario(),
                    request.formato(),
                    request.periodos()
            );
            AprovacaoAcaoPayload occPayload = buildPayload(
                    idempotencyKey + "-occ-" + occStart.toEpochMilli(), occRequest);
            filha.setActionPayloadJson(aprovacaoAcaoPayloadMapper.toJson(occPayload));

            aprovacaoRepository.save(filha);
        }
    }

    private AprovacaoAcaoPayload buildPayload(String idempotencyKey, CriarEventoRequest request) {
        UUID primaryAtivoId = (request.ativoIds() != null && !request.ativoIds().isEmpty())
                ? request.ativoIds().get(0)
                : null;
        return new AprovacaoAcaoPayload(
                idempotencyKey,
                null, // eventoId
                request.titulo(),
                request.descricao(),
                request.categoria(),
                request.grupoParoquialResponsavelId(),
                request.projetoId(),
                request.inicio(),
                request.fim(),
                request.status(),
                request.adicionadoExtraJustificativa(),
                null, // canceladoMotivo
                null, // motivo
                request.participantes(),
                request.localId(),
                request.localIds(),
                request.igrejaVinculoId(),
                primaryAtivoId,
                null, // ativoNome
                request.ativoIds(),
                request.adicionadoExtraJustificativa(),
                request.tipoHorario(),
                request.formato(),
                request.periodos());
    }
}
