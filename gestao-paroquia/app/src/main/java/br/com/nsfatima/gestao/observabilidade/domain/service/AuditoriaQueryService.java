package br.com.nsfatima.gestao.observabilidade.domain.service;

import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.AuditoriaOperacaoResponse;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Serviço de leitura da trilha de auditoria: expõe os registros persistidos por
 * {@link AuditoriaPersistenceService} em formato pronto para apresentação (API de trilha de
 * auditoria/dashboard), traduzindo o JSON opaco de detalhes de volta para um mapa estruturado.
 * Somente leitura — não participa da decisão de o que é auditável, apenas consulta o que já foi
 * gravado.
 */
@Service
public class AuditoriaQueryService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;
    private final ObjectMapper objectMapper;

    public AuditoriaQueryService(
            AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository,
            ObjectMapper objectMapper) {
        this.auditoriaOperacaoJpaRepository = auditoriaOperacaoJpaRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Busca a trilha de auditoria de uma organização em um intervalo de tempo, com filtros
     * opcionais por ator, ação, resultado e correlationId (todos ignorados quando nulos — ver
     * a query em {@link AuditoriaOperacaoJpaRepository#findForTrail}). Ordenado cronologicamente
     * para permitir reconstrução sequencial dos eventos de negócio.
     *
     * @param grupoParoquialId organização cujo histórico será consultado
     * @param inicio limite inferior (inclusivo) do intervalo, em UTC
     * @param fim limite superior (exclusivo) do intervalo, em UTC
     * @param ator filtro opcional por ator; {@code null} não filtra
     * @param acao filtro opcional por ação (case-insensitive); {@code null} não filtra
     * @param resultado filtro opcional por resultado (case-insensitive); {@code null} não filtra
     * @param correlationId filtro opcional para agrupar registros da mesma requisição/lote
     * @return itens da trilha de auditoria, com o JSON de detalhes já desserializado
     */
    @Transactional(readOnly = true)
    public List<AuditoriaOperacaoResponse.RegistroAuditavelItem> findAuditTrail(
            UUID grupoParoquialId,
            Instant inicio,
            Instant fim,
            String ator,
            String acao,
            String resultado,
            String correlationId) {
        return auditoriaOperacaoJpaRepository.findForTrail(
                        grupoParoquialId,
                        inicio,
                        fim,
                        ator,
                        acao,
                        resultado,
                        correlationId)
                .stream()
                .map(this::toItem)
                .toList();
    }

    private AuditoriaOperacaoResponse.RegistroAuditavelItem toItem(AuditoriaOperacaoEntity entity) {
        return new AuditoriaOperacaoResponse.RegistroAuditavelItem(
                entity.getId(),
                entity.getGrupoParoquialId(),
                entity.getRecursoTipo(),
                entity.getRecursoId(),
                entity.getAcao(),
                entity.getResultado(),
                entity.getAtor(),
                entity.getCorrelationId(),
                entity.getOcorridoEmUtc(),
                deserializeDetails(entity.getDetalhesAuditaveisJson()));
    }

    private Map<String, Object> deserializeDetails(String json) {
        try {
            return json == null || json.isBlank() ? Map.of() : objectMapper.readValue(json, MAP_TYPE);
        } catch (tools.jackson.core.JacksonException ex) {
            return Map.of("raw", json == null ? "" : json);
        }
    }
}
