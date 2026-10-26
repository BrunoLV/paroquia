package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.AprovacaoAtaAnterior;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/** Leitura e aprovação da ata da reunião anterior. */
public record AtaAnteriorRequest(
        UUID ataId,
        @NotNull AprovacaoAtaAnterior aprovacao,
        @Size(max = 2000) String ressalvas) {
}
