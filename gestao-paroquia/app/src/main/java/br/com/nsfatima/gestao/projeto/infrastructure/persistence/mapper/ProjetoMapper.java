package br.com.nsfatima.gestao.projeto.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoAprovacaoResponse;
import br.com.nsfatima.gestao.projeto.api.v1.dto.ProjetoResponse;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoEventoEntity;
import br.com.nsfatima.gestao.projeto.infrastructure.persistence.entity.ProjetoSolicitacaoAprovacaoEntity;
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

    public ProjetoAprovacaoResponse toAprovacaoResponse(ProjetoSolicitacaoAprovacaoEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ProjetoAprovacaoResponse(
                entity.getId(),
                entity.getProjetoId(),
                entity.getTipoSolicitacao(),
                entity.getStatus(),
                entity.getSolicitanteId(),
                entity.getCriadoEmUtc(),
                entity.getDecididoEmUtc(),
                entity.getActionPayloadJson(),
                entity.getCancelarEventosVinculados()
        );
    }
}

