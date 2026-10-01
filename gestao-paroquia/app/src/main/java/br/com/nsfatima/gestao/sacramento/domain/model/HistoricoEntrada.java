package br.com.nsfatima.gestao.sacramento.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Linha imutável da trilha estrita do assento (Cân. 535 §4): quem, com qual perfil, de onde, o quê
 * e os valores prévios/posteriores serializados.
 */
public record HistoricoEntrada(
        UUID id, UUID assentoId, String acao, UUID usuarioId, String ator, String perfil, String enderecoIp,
        String valoresAnteriores, String valoresPosteriores, Instant ocorridoEmUtc) {
}
