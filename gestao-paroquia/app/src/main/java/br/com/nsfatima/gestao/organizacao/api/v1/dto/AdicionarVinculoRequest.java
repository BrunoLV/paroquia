package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.PapelOrganizacional;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Requisição para vincular um usuário a uma organização com um determinado papel. O campo
 * {@code igrejaVinculoId} identifica a Matriz/Comunidade de contexto, obrigatória para pastorais e
 * movimentos por não terem hierarquia territorial própria.
 */
public record AdicionarVinculoRequest(
        @NotNull UUID organizacaoId,
        @NotNull TipoOrganizacao tipo,
        @NotNull PapelOrganizacional papel,
        UUID igrejaVinculoId
) {
    /**
     * Alias semântico de {@link #igrejaVinculoId()} para uso em contextos onde o nome "comunidade" é mais
     * claro que "igreja de vínculo".
     *
     * @return o identificador da igreja/comunidade de contexto
     */
    public UUID communityId() {
        return igrejaVinculoId;
    }
}
