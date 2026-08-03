package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Define o alcance de uma edição sobre um evento que pertence a uma série recorrente, determinando
 * se a alteração afeta apenas a ocorrência selecionada, a ocorrência e as futuras (com divisão da
 * série), ou toda a série recorrente.
 */
@Schema(description = "Escopo da edição para eventos recorrentes")
public enum EventoEditScope {
    @Schema(description = "Edita apenas esta instância específica")
    ONLY_THIS,

    @Schema(description = "Edita esta instância e todas as futuras (divide a série)")
    THIS_AND_FOLLOWING,

    @Schema(description = "Edita todas as instâncias da série")
    EDIT_ALL
}
