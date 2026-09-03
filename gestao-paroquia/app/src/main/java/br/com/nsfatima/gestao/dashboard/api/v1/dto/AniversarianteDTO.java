package br.com.nsfatima.gestao.dashboard.api.v1.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Aniversariante do período para avisos de missa e felicitações. Por LGPD, expõe apenas o
 * primeiro nome e o dia/mês — nunca o ano, a data completa ou o sobrenome — e só inclui fiéis
 * que autorizaram a divulgação do aniversário.
 *
 * @param primeiroNome primeiro nome do fiel
 * @param dia dia do mês do aniversário (1-31)
 * @param mes mês do aniversário (1-12)
 */
@Schema(description = "Aniversariante do período (dados mínimos por LGPD)")
public record AniversarianteDTO(
        String primeiroNome,
        int dia,
        int mes
) {
}
