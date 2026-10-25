package br.com.nsfatima.gestao.fiel.api.v1.dto;

import java.time.Instant;
import java.util.UUID;

/**
 * Payload estruturado interoperável para cumprimento do direito de portabilidade de dados
 * pessoais do titular (Artigo 18, inciso V da Lei Geral de Proteção de Dados - Lei 13.709/2018).
 */
public record PortabilidadeTitularResponse(
        UUID fielId,
        Instant exportadoEmUtc,
        String versaoFormato,
        String finalidade,
        FielResponse dadosCadastrais
) {
    public static PortabilidadeTitularResponse de(FielResponse dados) {
        return new PortabilidadeTitularResponse(
                dados.fielId(),
                Instant.now(),
                "1.0-JSON",
                "Portabilidade de dados pessoais (Art. 18, V da Lei 13.709/2018)",
                dados
        );
    }
}
