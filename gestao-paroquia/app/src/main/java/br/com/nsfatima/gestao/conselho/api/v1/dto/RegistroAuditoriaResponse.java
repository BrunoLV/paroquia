package br.com.nsfatima.gestao.conselho.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Entrada da trilha de auditoria exposta ao Pároco e à secretaria paroquial: quem, com qual perfil, o quê
 * e quando. O endereço IP e os estados serializados ficam apenas no banco, para perícia.
 */
public record RegistroAuditoriaResponse(UUID id, String acao, String ator, String perfil, Instant ocorridoEmUtc) {
}
