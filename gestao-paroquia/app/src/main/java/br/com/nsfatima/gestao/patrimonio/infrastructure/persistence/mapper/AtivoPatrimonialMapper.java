package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.mapper;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonial;
import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoPatrimonialId;
import br.com.nsfatima.gestao.patrimonio.domain.model.CodigoTombamento;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import org.springframework.stereotype.Component;

/**
 * Tradutor entre o agregado de domínio {@link AtivoPatrimonial} e a entidade JPA
 * {@link AtivoPatrimonialEntity}. O agregado cobre todos os campos da entidade, então a conversão
 * é um round-trip direto (o controle de versão otimista é feito pelo JPA via {@code @Version}).
 */
@Component
public class AtivoPatrimonialMapper {

    public AtivoPatrimonial toDomain(AtivoPatrimonialEntity entity) {
        if (entity == null) {
            return null;
        }
        return new AtivoPatrimonial(
                new AtivoPatrimonialId(entity.getId()),
                entity.getNome(),
                new CodigoTombamento(entity.getCodigoTombamento()),
                entity.getDescricao(),
                entity.getCategoria(),
                entity.getDataAquisicao(),
                entity.getValorAquisicao(),
                entity.getGrupoParoquialId(),
                entity.isReservavel(),
                entity.getStatus(),
                entity.getCadastradoPorId(),
                entity.getCriadoEmUtc(),
                entity.getUpdatedAt(),
                entity.getDeletedAt(),
                entity.getVersion());
    }

    public AtivoPatrimonialEntity toEntity(AtivoPatrimonial domain) {
        return new AtivoPatrimonialEntity(
                domain.getId().value(),
                domain.getNome(),
                domain.getCodigoTombamento().valor(),
                domain.getDescricao(),
                domain.getCategoria(),
                domain.getStatus(),
                domain.isReservavel(),
                domain.getGrupoParoquialId(),
                domain.getDataAquisicao(),
                domain.getValorAquisicao(),
                domain.getCadastradoPorId(),
                domain.getCreatedAt(),
                domain.getUpdatedAt(),
                domain.getDeletedAt(),
                domain.getVersion());
    }
}
