package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencao;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoManutencaoId;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.PeriodoManutencao;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoManutencaoEntity;
import org.springframework.stereotype.Component;

/** Tradutor entre o agregado {@link AtivoManutencao} e a entidade JPA {@link AtivoManutencaoEntity}. */
@Component
public class AtivoManutencaoMapper {

    public AtivoManutencao toDomain(AtivoManutencaoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AtivoManutencao(
                new AtivoManutencaoId(entity.getId()),
                new AtivoPatrimonialId(entity.getAtivoId()),
                entity.getDescricaoReparo(),
                new PeriodoManutencao(entity.getDataInicio(), entity.getDataFimPrevista()),
                entity.getDataFimReal(),
                entity.getRegistradoPorId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion());
    }

    public AtivoManutencaoEntity toEntity(AtivoManutencao domain) {
        return new AtivoManutencaoEntity(
                domain.getId().value(),
                domain.getAtivoId().value(),
                domain.getDescricaoReparo(),
                domain.getPeriodo().inicio(),
                domain.getPeriodo().fimPrevista(),
                domain.getDataFimReal(),
                domain.getRegistradoPorId(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getVersion());
    }
}
