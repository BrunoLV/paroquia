package br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.domain.model.ProjetoStatus;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import org.springframework.stereotype.Component;

@Component
public class ProjetoMapper {

    public ProjetoResponse toResponse(ProjetoEventoEntity entity) {
        return toResponse(entity, false);
    }

    public ProjetoResponse toResponse(ProjetoEventoEntity entity, boolean updated) {
        if (entity == null) {
            return null;
        }
        return new ProjetoResponse(
                entity.getId(),
                entity.getNome(),
                entity.getDescricao(),
                entity.getOrganizacaoResponsavelId(),
                entity.getInicioUtc(),
                entity.getFimUtc(),
                entity.getStatusEnum(),
                updated
        );
    }
}
