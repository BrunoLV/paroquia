package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Dados públicos de uma organização, expostos pela API de gestão de organizações.
 */
@Schema(description = "Dados de uma organização")
public record OrganizacaoResponse(
        @Schema(description = "Identificador único")
        UUID id,
        @Schema(description = "Nome da organização")
        String nome,
        @Schema(description = "Tipo da organização")
        TipoOrganizacao tipo,
        @Schema(description = "Informação de contato")
        String contato,
        @Schema(description = "Nome do coordenador")
        String coordenador,
        @Schema(description = "Nome do vice-coordenador")
        String viceCoordenador,
        @Schema(description = "Indica se a organização está ativa")
        boolean ativo,
        @Schema(description = "Identificador da organização-mãe")
        UUID parentId,
        @Schema(description = "Identificador da Pastoral/Movimento Oficial")
        UUID pastoralOficialId,
        @Schema(description = "Múnus a que serve")
        SetorPastoral setorPastoral,
        @Schema(description = "Natureza do conselho (somente CONSELHO)")
        SubtipoConselho subtipoConselho,
        @Schema(description = "E-mail institucional")
        String email,
        @Schema(description = "Telefone institucional")
        String telefone
) {
}
