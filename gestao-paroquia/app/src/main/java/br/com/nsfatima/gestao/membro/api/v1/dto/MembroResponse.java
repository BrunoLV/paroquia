package br.com.nsfatima.gestao.membro.api.v1.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Representação do vínculo usuário-organização já enriquecida com os dados de exibição do usuário e da
 * organização (nome, e-mail, telefone), resolvidos em {@code MembroMapper} a partir dos respectivos contextos.
 */
public record MembroResponse(
        UUID id,
        UUID usuarioId,
        String usuarioNome,
        String usuarioEmail,
        String usuarioTelefone,
        UUID organizacaoId,
        String organizacaoNome,
        String papel,
        LocalDate dataEntrada,
        boolean ativo,
        UUID igrejaId
) {
}
