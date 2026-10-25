package br.com.nsfatima.gestao.conselho.domain.model;

import java.util.UUID;

/**
 * Porta mínima de numeração: entrega o próximo número sequencial de ata de uma organização no ano. A ata a
 * recebe no momento de selar, para que "selada" e "numerada" nunca se separem.
 */
@FunctionalInterface
public interface NumeradorAta {

    /**
     * Reserva e devolve o próximo número da série da organização no ano.
     *
     * @param organizacaoConvocanteId organização dona da série de atas
     * @param ano ano civil da série
     * @return número sequencial, começando em 1
     */
    int proximo(UUID organizacaoConvocanteId, int ano);
}
