package br.com.nsfatima.gestao.patrimonio.domain.event;

import java.util.UUID;

/**
 * Publicado após a exclusão lógica de um ativo, para que consumidores interessados (auditoria,
 * caches de disponibilidade) reajam à saída do bem do inventário operacional.
 */
public record AtivoExcluidoEvent(UUID ativoId) {
}
