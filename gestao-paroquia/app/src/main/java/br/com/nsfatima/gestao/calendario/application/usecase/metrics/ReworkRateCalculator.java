package br.com.nsfatima.gestao.calendario.application.usecase.metrics;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.IndicadorRetrabalhoResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ReworkRateCalculator {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;
    private final ObjectMapper objectMapper;

    public ReworkRateCalculator(
            AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository,
            ObjectMapper objectMapper) {
        this.auditoriaOperacaoJpaRepository = auditoriaOperacaoJpaRepository;
        this.objectMapper = objectMapper;
    }

    public IndicadorRetrabalhoResponse calculateForOrganization(
            UUID organizacaoId,
            PeriodoOperacionalPolicy.ResolvedPeriod periodo) {
        long numerador = auditoriaOperacaoJpaRepository.countEligibleAdministrativeReworkOccurrences(
                organizacaoId,
                periodo.inicio(),
                periodo.fim());
        long denominador = auditoriaOperacaoJpaRepository.countDistinctAffectedEvents(
                organizacaoId,
                periodo.inicio(),
                periodo.fim());
        double taxa = denominador == 0L ? 0.0 : numerador / (double) denominador;
        return new IndicadorRetrabalhoResponse(
                organizacaoId,
                periodo.toResponse(),
                taxa,
                numerador,
                denominador);
    }

    public SnapshotReworkMetrics calculateForSnapshot(PeriodoOperacionalPolicy.ResolvedPeriod periodo) {
        return calculate(auditoriaOperacaoJpaRepository
                .findByOcorridoEmUtcGreaterThanEqualAndOcorridoEmUtcLessThanOrderByOcorridoEmUtcAscIdAsc(
                        periodo.inicio(),
                        periodo.fim()));
    }

    private SnapshotReworkMetrics calculate(List<AuditoriaOperacaoEntity> auditRecords) {
        Set<UUID> distinctAffectedEvents = new HashSet<>();
        long numerador = 0L;

        for (AuditoriaOperacaoEntity auditRecord : auditRecords) {
            if (auditRecord.getEventoId() != null) {
                distinctAffectedEvents.add(auditRecord.getEventoId());
            }

            if (isEligibleAdministrativeRework(auditRecord)) {
                numerador++;
            }
        }

        long denominador = distinctAffectedEvents.size();
        double taxa = denominador == 0L ? 0.0 : numerador / (double) denominador;
        return new SnapshotReworkMetrics(taxa, numerador, denominador);
    }

    private boolean isEligibleAdministrativeRework(AuditoriaOperacaoEntity auditRecord) {
        String action = auditRecord.getAcao() == null ? "" : auditRecord.getAcao().toLowerCase();
        String result = auditRecord.getResultado() == null ? "" : auditRecord.getResultado().toLowerCase();
        if (!result.equals("success") && !result.equals("executed")) {
            return false;
        }

        if (action.equals("cancel")) {
            return true;
        }

        if (!action.equals("patch")) {
            return false;
        }

        Map<String, Object> details = deserializeDetails(auditRecord.getDetalhesAuditaveisJson());
        return isTrue(details.get("scheduleChanged"))
                || isTrue(details.get("responsibleOrgChanged"))
                || isTrue(details.get("cancellation"));
    }

    private Map<String, Object> deserializeDetails(String json) {
        try {
            return json == null || json.isBlank() ? Map.of() : objectMapper.readValue(json, MAP_TYPE);
        } catch (IOException ex) {
            return Map.of();
        }
    }

    private boolean isTrue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    public record SnapshotReworkMetrics(
            double taxaRetrabalho,
            long numeradorOcorrenciasElegiveis,
            long denominadorEventosAfetados) {
    }
}
