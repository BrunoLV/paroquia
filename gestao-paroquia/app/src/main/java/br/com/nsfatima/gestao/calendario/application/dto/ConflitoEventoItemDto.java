package br.com.nsfatima.gestao.calendario.application.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Representa um conflito individual detectado (de espaço físico, item patrimonial ou agenda eclesial)
 * para exibição analítica e subsídio à deliberação do Conselho e Pároco.
 */
public record ConflitoEventoItemDto(
        String tipoConflito,
        UUID recursoId,
        String recursoNome,
        UUID eventoConcorrenteId,
        String eventoConcorrenteTitulo,
        Instant inicioUtc,
        Instant fimUtc,
        String solicitanteOuResponsavel,
        int prioridadeConcorrente,
        int prioridadeSolicitada,
        String detalhe
) {}
