package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.AcaoAta;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/** Linha da listagem de atas. */
public record AtaResumoResponse(
        UUID id,
        TipoReuniao tipo,
        String tipoRotulo,
        String titulo,
        LocalDate dataReuniao,
        UUID organizacaoConvocanteId,
        String organizacaoConvocante,
        StatusAta status,
        String numero,
        Set<AcaoAta> acoes) {
}
