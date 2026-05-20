package br.com.nsfatima.gestao.calendario.api.v1.dto.evento;

import br.com.nsfatima.gestao.calendario.domain.type.PapelEnvolvido;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EventoEnvolvidoInput(
        @NotNull UUID organizacaoId,
        @NotNull PapelEnvolvido papel) {
}
