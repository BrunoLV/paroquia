package br.com.nsfatima.gestao.conselho.domain.model;

import java.time.Instant;
import java.util.UUID;

/**
 * Linha imutável da trilha de auditoria da ata: quem agiu, com qual perfil, de qual endereço, o que fez e o
 * estado anterior e posterior serializados. Nas exportações, o estado posterior carrega os parâmetros do
 * documento gerado (formato, layout, situação da ata).
 */
public record RegistroAuditoriaAta(
        UUID id,
        AtaReuniaoId ataId,
        String acao,
        UUID usuarioId,
        String ator,
        String perfil,
        String enderecoIp,
        String valoresAnteriores,
        String valoresPosteriores,
        Instant ocorridoEmUtc) {
}
