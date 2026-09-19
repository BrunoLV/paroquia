package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Define o alcance de uma edição sobre um evento que pertence a uma série recorrente, determinando
 * se a alteração afeta apenas a ocorrência selecionada, a ocorrência e as futuras (com divisão da
 * série), ou toda a série recorrente.
 */
@Schema(description = "Escopo da edição para eventos recorrentes")
public enum EscopoEdicaoEvento {
    @Schema(description = "Edita apenas esta instância específica")
    SOMENTE_ESTE,

    @Schema(description = "Edita esta instância e todas as futuras (divide a série)")
    ESTE_E_PROXIMOS,

    @Schema(description = "Edita todas as instâncias da série")
    TODOS
}
