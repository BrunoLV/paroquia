package br.com.nsfatima.gestao.fiel.api.v1.dto;

/**
 * Requisição para alteração e revogação das preferências e consentimentos secundários
 * pelo titular (Art. 8º, § 5º da LGPD).
 */
public record PreferenciasPrivacidadeRequest(
        Boolean usoImagemAutorizado,
        Boolean permitirDivulgacaoAniversario
) {
}
