package br.com.nsfatima.gestao.importacao.api.v1.dto;

import br.com.nsfatima.gestao.importacao.application.service.LeitorArquivoImportacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Pedido de confirmação da importação. O cliente reenvia as linhas, então nada nele é confiável: cada linha
 * volta a passar pelo mesmo parse e pelas mesmas regras de domínio da validação.
 *
 * @param itens linhas a gravar, limitadas ao mesmo máximo aceito no upload
 */
public record ImportacaoConfirmacaoRequest(
        @Schema(description = "Linhas a gravar")
        @NotEmpty @Size(max = LeitorArquivoImportacao.MAX_LINHAS) List<@NotNull @Valid ImportacaoLinhaDTO> itens
) {
}
