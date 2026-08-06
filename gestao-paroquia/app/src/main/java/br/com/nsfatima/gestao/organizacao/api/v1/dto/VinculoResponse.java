package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import java.util.UUID;

/**
 * Representa um vínculo ativo entre um usuário e uma organização, com nomes de organização e de
 * igreja/comunidade de contexto já resolvidos para exibição, usado por {@code ListarVinculosUseCase} para
 * determinar o escopo de acesso do usuário.
 */
public record VinculoResponse(
        UUID id,
        UUID grupoParoquialId,
        String grupoParoquialNome,
        String tipoGrupoParoquial,
        String papel,
        boolean ativo,
        UUID igrejaVinculoId,
        String igrejaVinculoNome
) {
}
