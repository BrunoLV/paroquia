package br.com.nsfatima.gestao.fiel.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.fiel.api.v1.dto.FielResponse;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.FielEntity;
import org.springframework.stereotype.Component;

@Component
public class FielMapper {

    public FielResponse toResponse(FielEntity entity) {
        if (entity == null) {
            return null;
        }
        return new FielResponse(
                entity.getId(),
                entity.getNome(),
                entity.getCpf(),
                entity.getDataNascimento(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getEndereco(),
                entity.getVocacao(),
                entity.isAtivo(),
                entity.isBatizado(),
                entity.isPrimeiraComunhao(),
                entity.isCrismado(),
                entity.isCasadoIgrejaCatolica()
        );
    }
}
