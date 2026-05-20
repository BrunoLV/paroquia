package br.com.nsfatima.gestao.membro.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.membro.api.v1.dto.ParticipacaoResponse;
import br.com.nsfatima.gestao.membro.api.v1.dto.MembroResponse;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.ParticipanteOrganizacaoEntity;
import org.springframework.stereotype.Component;

@Component
public class MembroMapper {

    public MembroResponse toResponse(MembroEntity entity) {
        if (entity == null) return null;
        return new MembroResponse(
                entity.getId(),
                entity.getNomeCompleto(),
                entity.getDataNascimento(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getEndereco(),
                entity.getUsuarioId(),
                entity.isAtivo(),
                entity.getDataBatismo(),
                entity.getLocalBatismo(),
                entity.getDataCrisma(),
                entity.getDataMatrimonio()
        );
    }

    public ParticipacaoResponse toParticipacaoResponse(ParticipanteOrganizacaoEntity entity) {
        if (entity == null) return null;
        return new ParticipacaoResponse(
                entity.getId(),
                entity.getMembroId(),
                entity.getOrganizacaoId(),
                entity.getDataInicio(),
                entity.getDataFim(),
                entity.isAtivo()
        );
    }
}
