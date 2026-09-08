package br.com.nsfatima.gestao.reserva.api.v1.dto;

import java.util.List;
import java.util.UUID;

public record ReservaRecorrenteResponse(
        UUID grupoRecorrenciaId,
        List<ReservaResponse> reservasCriadas,
        List<ConflitoDetalheResponse> conflitosDescartados
) {}
