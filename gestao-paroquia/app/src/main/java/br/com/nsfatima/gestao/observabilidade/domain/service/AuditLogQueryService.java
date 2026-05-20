package br.com.nsfatima.gestao.observabilidade.domain.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import br.com.nsfatima.gestao.calendario.api.v1.dto.metrics.AuditoriaOperacaoResponse;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.entity.AuditoriaOperacaoEntity;
import br.com.nsfatima.gestao.observabilidade.infrastructure.persistence.repository.AuditoriaOperacaoJpaRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogQueryService {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository;
    private final ObjectMapper objectMapper;

    public AuditLogQueryService(
            AuditoriaOperacaoJpaRepository auditoriaOperacaoJpaRepository,
            ObjectMapper objectMapper) {
        this.auditoriaOperacaoJpaRepository = auditoriaOperacaoJpaRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<AuditoriaOperacaoResponse.RegistroAuditavelItem> findAuditTrail(
            UUID organizacaoId,
            Instant inicio,
            Instant fim,
            String ator,
            String acao,
            String resultado,
            String correlationId) {
        return auditoriaOperacaoJpaRepository.findForTrail(
                organizacaoId,
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
                entity.getOrganizacaoId(),
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
        } catch (IOException ex) {
            return Map.of("raw", json == null ? "" : json);
        }
    }
}
