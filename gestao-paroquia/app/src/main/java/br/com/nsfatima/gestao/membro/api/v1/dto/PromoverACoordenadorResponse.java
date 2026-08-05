package br.com.nsfatima.gestao.membro.api.v1.dto;

import java.util.UUID;

/**
 * Resultado da promoção de um {@code Fiel} a um papel de liderança: o vínculo atualizado e, se um novo
 * {@code Usuario} foi criado nesta operação, seu identificador.
 */
public record PromoverACoordenadorResponse(
        UUID membroId,
        UUID fielId,
        UUID usuarioId,
        boolean usuarioCriado
) {
}
