package br.com.nsfatima.gestao.calendario.application.service;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.IndicadorRetrabalhoResponse;
import br.com.nsfatima.gestao.calendario.domain.policy.PeriodoOperacionalPolicy;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.springframework.stereotype.Component;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Calcula a taxa de retrabalho administrativo sobre eventos: a proporção entre
 * ocorrências de retrabalho elegíveis (cancelamentos, ou atualizações que alteraram
 * agenda, organização responsável, ou representaram um cancelamento efetivo, conforme
 * os detalhes auditáveis da operação) e o total de eventos distintos afetados por
 * qualquer operação auditada no período. Uma taxa alta indica que decisões operacionais
 * (agenda, responsável) estão sendo revertidas ou refeitas com frequência incomum.
 * A fonte de dados é o log de auditoria ({@code AuditoriaOperacaoEntity}), não o estado
 * atual dos eventos, então o cálculo reflete o histórico de operações, não o snapshot.
 */
@Component
public class CalculadoraTaxaRetrabalho {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };
    private static final String RESULTADO_SUCESSO = "sucesso";
    private static final String RESULTADO_EXECUTADO = "executado";
    private static final String ACAO_CANCELAMENTO = "cancelar";
    private static final String ACAO_ALTERACAO = "atualizar";

    private final AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;
    private final ObjectMapper objectMapper;

    public CalculadoraTaxaRetrabalho(
            AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository,
            ObjectMapper objectMapper) {
        this.auditoriaOperacaoJpaRepository = auditoriaOperacaoJpaRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Calcula a taxa de retrabalho para uma organização específica dentro do período
     * resolvido, consultando diretamente o banco (contagens agregadas) em vez de
     * carregar e iterar os registros de auditoria em memória. A taxa é o quociente entre
     * ocorrências de retrabalho administrativo elegíveis e o total de eventos distintos
     * afetados por operações auditadas no período; retorna {@code 0.0} quando não há
     * eventos afetados, para evitar divisão por zero.
     *
     * @param organizacaoId organização cujas operações auditadas serão consideradas
     * @param periodo período operacional já resolvido (início/fim) para o cálculo
     * @return indicador com a taxa calculada e os valores de numerador/denominador
     */
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

    /**
     * Calcula a taxa de retrabalho em nível global (todas as organizações) para o
     * período informado, carregando os registros de auditoria em memória para compor
     * um snapshot consolidado. Usado quando o indicador é reportado sem segmentação
     * por organização (ex.: dashboards agregados).
     *
     * @param periodo período operacional já resolvido (início/fim) para o cálculo
     * @return métricas de retrabalho consolidadas para o período
     */
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
        if (!result.equals(RESULTADO_SUCESSO) && !result.equals(RESULTADO_EXECUTADO)) {
            return false;
        }

        if (action.equals(ACAO_CANCELAMENTO)) {
            return true;
        }

        if (!action.equals(ACAO_ALTERACAO)) {
            return false;
        }

        Map<String, Object> details = deserializeDetails(auditRecord.getDetalhesAuditaveisJson());
        return isTrue(details.get("agendaAlterada"))
                || isTrue(details.get("organizacaoResponsavelAlterada"))
                || isTrue(details.get("cancelamento"));
    }

    private Map<String, Object> deserializeDetails(String json) {
        try {
            return json == null || json.isBlank() ? Map.of() : objectMapper.readValue(json, MAP_TYPE);
        } catch (tools.jackson.core.JacksonException ex) {
            return Map.of();
        }
    }

    private boolean isTrue(Object value) {
        if (value instanceof Boolean bool) {
            return bool;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

    /**
     * Métricas consolidadas de retrabalho para um snapshot de período: a taxa
     * (numerador dividido pelo denominador, ou zero se não houver eventos afetados)
     * junto com os valores brutos que a compõem, para permitir auditoria do cálculo.
     *
     * @param taxaRetrabalho proporção de ocorrências de retrabalho elegíveis sobre o
     *                        total de eventos distintos afetados no período
     * @param numeradorOcorrenciasElegiveis quantidade de ocorrências de retrabalho
     *                                        administrativo elegíveis (cancelamentos e
     *                                        atualizações qualificadas)
     * @param denominadorEventosAfetados quantidade de eventos distintos afetados por
     *                                    qualquer operação auditada no período
     */
    public record SnapshotReworkMetrics(
            double taxaRetrabalho,
            long numeradorOcorrenciasElegiveis,
            long denominadorEventosAfetados) {
    }
}
