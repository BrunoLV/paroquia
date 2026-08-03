package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Decisão de homologação a aplicar sobre uma ata/decisão de conselho. {@code motivoVeto} é
 * opcional na validação estrutural do DTO, mas é obrigatório em tempo de execução quando
 * {@code status} é {@code VETADO} — a regra é reforçada em
 * {@link br.com.nsfatima.gestao.conselho.application.usecase.HomologarCpcDecisaoUseCase}.
 */
public record HomologarCpcDecisaoRequest(
        @NotNull StatusHomologacao status,
        @Size(max = 2000) String motivoVeto
) {
}
