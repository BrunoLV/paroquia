package br.com.nsfatima.gestao.membro.api.v1.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Representação do vínculo Fiel-organização já enriquecida com os dados de exibição do Fiel e da organização
 * (nome, e-mail de contato, telefone), resolvidos em {@code MembroMapper} diretamente do {@code Fiel}
 * vinculado — sem depender de um {@code Usuario}, que pode nem existir.
 */
public record MembroResponse(
        UUID id,
        UUID fielId,
        String fielNome,
        String fielEmail,
        String fielTelefone,
        UUID organizacaoId,
        String organizacaoNome,
        String papel,
        LocalDate dataEntrada,
        boolean ativo,
        UUID igrejaId
) {
}
