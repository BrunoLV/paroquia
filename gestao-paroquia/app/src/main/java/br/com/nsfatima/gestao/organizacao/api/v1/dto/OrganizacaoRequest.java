package br.com.nsfatima.gestao.organizacao.api.v1.dto;

import br.com.nsfatima.gestao.organizacao.domain.model.SetorPastoral;
import br.com.nsfatima.gestao.organizacao.domain.model.SubtipoConselho;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoOrganizacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Payload de criação/atualização de uma organização. A combinação de {@code tipo}, {@code parentId} e
 * {@code pastoralOficialId} é validada por {@code OrganizacaoService.validateHierarchy} contra as regras da
 * hierarquia organizacional (ex.: Pastoral/Movimento exige {@code pastoralOficialId} e organização-mãe do
 * tipo {@code CONSELHO_PASTORAL}).
 */
@Schema(description = "Requisição para criar ou atualizar uma organização")
public record OrganizacaoRequest(
        @Schema(description = "Nome da organização", example = "Pastoral da Juventude")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 255, message = "O nome não pode exceder 255 caracteres")
        String nome,

        @Schema(description = "Tipo da organização")
        @NotNull(message = "O tipo é obrigatório")
        TipoOrganizacao tipo,

        @Schema(description = "Informação de contato", example = "pj@nsfatima.com")
        @Size(max = 255, message = "O contato não pode exceder 255 caracteres")
        String contato,

        @Schema(description = "Nome do coordenador", example = "João Silva")
        @Size(max = 255, message = "O nome do coordenador não pode exceder 255 caracteres")
        String coordenador,

        @Schema(description = "Nome do vice-coordenador", example = "Maria Santos")
        @Size(max = 255, message = "O nome do vice-coordenador não pode exceder 255 caracteres")
        String viceCoordenador,

        @Schema(description = "Indica se a organização está ativa", example = "true")
        boolean ativo,

        @Schema(description = "Identificador da organização-mãe")
        UUID parentId,

        @Schema(description = "Identificador da Pastoral/Movimento Oficial")
        UUID pastoralOficialId,

        @Schema(description = "Múnus a que serve: ensinar, santificar ou governar (obrigatório para PASTORAL e "
                + "SERVICO_PAROQUIAL)")
        SetorPastoral setorPastoral,

        @Schema(description = "Natureza do conselho: CPP, CPC ou CAEP (obrigatória para CONSELHO)")
        SubtipoConselho subtipoConselho,

        @Schema(description = "E-mail institucional", example = "pj@nsfatima.com")
        @Email(message = "E-mail inválido")
        @Size(max = 255, message = "O e-mail não pode exceder 255 caracteres")
        String email,

        @Schema(description = "Telefone institucional", example = "(11) 99999-9999")
        @Size(max = 32, message = "O telefone não pode exceder 32 caracteres")
        String telefone
) {
}
