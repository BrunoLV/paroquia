package br.com.nsfatima.gestao.aprovacao.application.service;

import br.com.nsfatima.gestao.aprovacao.application.dto.AprovacaoAcaoPayload;
import br.com.nsfatima.gestao.aprovacao.application.mapper.AprovacaoAcaoPayloadMapper;
import br.com.nsfatima.gestao.aprovacao.domain.model.AprovacaoStatus;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.repository.AprovacaoJpaRepository;
import br.com.nsfatima.gestao.calendario.application.dto.ConflitoEventoItemDto;
import br.com.nsfatima.gestao.calendario.application.service.DetectorConflitoEventoService;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Serviço responsável pelo recálculo automático em cadeia dos conflitos analíticos
 * de todas as solicitações de aprovação pendentes que concorram por recursos paroquiais.
 *
 * <p>Invariante: Sempre que uma solicitação é aprovada e suas reservas confirmadas no banco,
 * as demais solicitações que disputem os mesmos espaços ou bens materiais devem ter seus
 * conflitos recalculados imediatamente para subsidiar a deliberação pastoral e bloquear aprovações
 * indevidas.</p>
 */
@Service
public class AprovacaoRecalculoConflitoService {

    private static final Logger log = LoggerFactory.getLogger(AprovacaoRecalculoConflitoService.class);

    private final AprovacaoJpaRepository aprovacaoJpaRepository;
    private final DetectorConflitoEventoService detectorConflitoEventoService;
    private final AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper;
    private final ObjectMapper objectMapper;

    public AprovacaoRecalculoConflitoService(
            AprovacaoJpaRepository aprovacaoJpaRepository,
            DetectorConflitoEventoService detectorConflitoEventoService,
            AprovacaoAcaoPayloadMapper aprovacaoAcaoPayloadMapper,
            ObjectMapper objectMapper) {
        this.aprovacaoJpaRepository =
                Objects.requireNonNull(aprovacaoJpaRepository, "AprovacaoJpaRepository é obrigatório");
        this.detectorConflitoEventoService =
                Objects.requireNonNull(detectorConflitoEventoService, "DetectorConflitoEventoService é obrigatório");
        this.aprovacaoAcaoPayloadMapper =
                Objects.requireNonNull(aprovacaoAcaoPayloadMapper, "AprovacaoAcaoPayloadMapper é obrigatório");
        this.objectMapper = Objects.requireNonNull(objectMapper, "ObjectMapper é obrigatório");
    }

    /**
     * Recalcula exaustivamente os conflitos de todas as solicitações pendentes após a aprovação de uma solicitação.
     *
     * @param aprovacaoAprovadaId ID da solicitação recém-aprovada para ignorar na varredura de pendências.
     */
    @Transactional
    public void recalcularConflitosPendentes(UUID aprovacaoAprovadaId) {
        List<AprovacaoEntity> pendentes =
                aprovacaoJpaRepository.findByStatusOrderByCriadoEmUtcAsc(AprovacaoStatus.PENDENTE);

        for (AprovacaoEntity pendente : pendentes) {
            if (aprovacaoAprovadaId != null && aprovacaoAprovadaId.equals(pendente.getId())) {
                continue;
            }
            if (pendente.getActionPayloadJson() == null || pendente.getActionPayloadJson().isBlank()) {
                continue;
            }

            try {
                AprovacaoAcaoPayload payload = aprovacaoAcaoPayloadMapper.toPayload(pendente.getActionPayloadJson());
                if (payload == null || payload.inicio() == null || payload.fim() == null) {
                    continue;
                }

                List<ConflitoEventoItemDto> novosConflitos = detectorConflitoEventoService.detectarConflitos(
                        payload.inicio(),
                        payload.fim(),
                        payload.localId(),
                        payload.localIds(),
                        payload.ativoIds(),
                        payload.categoria(),
                        payload.eventoId(),
                        payload.tipoHorario(),
                        payload.igrejaVinculoId()
                );

                String jsonAtualizado = objectMapper.writeValueAsString(novosConflitos);
                pendente.setConflitosJson(jsonAtualizado);
                aprovacaoJpaRepository.save(pendente);
            } catch (JacksonException ex) {
                log.warn("Falha ao serializar conflitos recalculados para a aprovação ID {}: {}",
                        pendente.getId(), ex.getMessage());
            } catch (RuntimeException ex) {
                log.warn("Erro ao recalcular conflitos para a solicitação de aprovação pendente ID {}: {}",
                        pendente.getId(), ex.getMessage());
            }
        }
    }
}
