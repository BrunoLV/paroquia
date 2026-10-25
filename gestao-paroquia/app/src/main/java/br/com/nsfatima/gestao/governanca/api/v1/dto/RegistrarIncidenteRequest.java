package br.com.nsfatima.gestao.governanca.api.v1.dto;

import br.com.nsfatima.gestao.governanca.domain.model.GravidadeIncidente;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

/**
 * Payload de requisição para formalização de um incidente de segurança (Art. 48 LGPD).
 */
@Schema(description = "Requisição para registro de incidente de segurança e violação de dados")
public record RegistrarIncidenteRequest(
        @Schema(description = "Título resumido da ocorrência", example = "Tentativa de acesso indevido à base de fiéis")
        @NotBlank(message = "Título é obrigatório.")
        @Size(max = 200, message = "Título deve ter no máximo 200 caracteres.")
        String titulo,

        @Schema(description = "Descrição detalhada do incidente e sistemas impactados")
        @NotBlank(message = "Descrição é obrigatória.")
        String descricao,

        @Schema(description = "Natureza dos dados pessoais afetados", example = "Dados cadastrais civis")
        @NotBlank(message = "Tipo de dado afetado é obrigatório.")
        @Size(max = 100, message = "Tipo de dado afetado deve ter no máximo 100 caracteres.")
        String tipoDadoAfetado,

        @Schema(description = "Classificação de gravidade avaliada", example = "MEDIA")
        @NotNull(message = "Gravidade é obrigatória.")
        GravidadeIncidente categoriaGravidade,

        @Schema(description = "Data e hora estimada da ocorrência em UTC")
        @NotNull(message = "Data da ocorrência é obrigatória.")
        Instant dataOcorrencia,

        @Schema(description = "Data e hora da detecção formal da violação em UTC")
        @NotNull(message = "Data de detecção é obrigatória.")
        Instant dataDeteccao,

        @Schema(description = "Medidas técnicas de contenção e mitigação adotadas")
        String medidasMitigacao
) {
}
