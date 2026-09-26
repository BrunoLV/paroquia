package br.com.nsfatima.gestao.clero.api.v1.dto;

import java.util.UUID;

public record DioceseResponse(
        UUID id,
        String nome,
        String bispoDiocesano,
        String cnpjMitra,
        String cidade,
        String uf,
        String contato,
        boolean ativo
) {
}
