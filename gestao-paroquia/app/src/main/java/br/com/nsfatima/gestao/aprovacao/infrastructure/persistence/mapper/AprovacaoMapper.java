package br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.aprovacao.api.v1.dto.AprovacaoResponse;
import br.com.nsfatima.gestao.aprovacao.infrastructure.persistence.entity.AprovacaoEntity;
import br.com.nsfatima.gestao.calendario.domain.type.TipoSolicitacaoResponse;
import br.com.nsfatima.gestao.calendario.infrastructure.observability.LegacyEnumInconsistencyPublisher;
import org.springframework.stereotype.Component;

/**
 * Converte {@link AprovacaoEntity} (persistência) em {@link AprovacaoResponse} (contrato de API), isolando o
 * modelo JPA do formato exposto externamente. Normaliza o tipo de solicitação legado via
 * {@link LegacyEnumInconsistencyPublisher}, reportando inconsistências de dados legados sem quebrar a resposta.
 */
@Component
public class AprovacaoMapper {

    private final LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher;

    public AprovacaoMapper(LegacyEnumInconsistencyPublisher legacyEnumInconsistencyPublisher) {
        this.legacyEnumInconsistencyPublisher = legacyEnumInconsistencyPublisher;
    }

    /**
     * Converte a entidade de aprovação persistida em seu DTO de resposta para a API.
     *
     * @param entity entidade de aprovação; pode ser {@code null}
     * @return DTO de resposta correspondente, ou {@code null} se a entidade for {@code null}
     */
    public AprovacaoResponse toResponse(AprovacaoEntity entity) {
        if (entity == null) {
            return null;
        }

        return new AprovacaoResponse(
                entity.getId(),
                entity.getEventoId(),
                TipoSolicitacaoResponse.fromStoredValue(
                        entity.getTipoSolicitacao(),
                        legacyEnumInconsistencyPublisher,
                        entity.getId().toString()),
                entity.getStatus().name(),
                entity.getAprovadorPapel(),
                entity.getCriadoEmUtc(),
                entity.getDecididoEmUtc(),
                entity.getSolicitanteId(),
                entity.getAprovadorId(),
                entity.getMensagemErroExecucao(),
                entity.getActionPayloadJson());
    }
}
