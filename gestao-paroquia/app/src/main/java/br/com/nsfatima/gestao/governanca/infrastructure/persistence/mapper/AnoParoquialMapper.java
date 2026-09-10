package br.com.nsfatima.gestao.governanca.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.governanca.domain.model.AnoParoquial;
import br.com.nsfatima.gestao.governanca.infrastructure.persistence.entity.AnoParoquialEntity;
import org.springframework.stereotype.Component;

/**
 * Converte entre o agregado {@link AnoParoquial} (domínio puro) e a {@link AnoParoquialEntity} JPA.
 */
@Component
public class AnoParoquialMapper {

    public AnoParoquial toDomain(AnoParoquialEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AnoParoquial(
                entity.getAno(),
                entity.getStatus(),
                entity.getDataFechamentoUtc(),
                entity.getUpdatedAt());
    }

    public AnoParoquialEntity toEntity(AnoParoquial domain) {
        AnoParoquialEntity entity = new AnoParoquialEntity();
        entity.setAno(domain.getAno());
        entity.setStatus(domain.getStatus());
        entity.setDataFechamentoUtc(domain.getDataFechamento());
        return entity;
    }
}
