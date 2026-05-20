package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Escopo da edição para eventos recorrentes")
public enum EventoEditScope {
    @Schema(description = "Edita apenas esta instância específica")
    ONLY_THIS,

    @Schema(description = "Edita esta instância e todas as futuras (divide a série)")
    THIS_AND_FOLLOWING
}
