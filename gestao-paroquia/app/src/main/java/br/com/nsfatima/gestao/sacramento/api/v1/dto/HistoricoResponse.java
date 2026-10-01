package br.com.nsfatima.gestao.sacramento.api.v1.dto;

import br.com.nsfatima.gestao.sacramento.domain.model.HistoricoEntrada;

import java.time.Instant;
import java.util.UUID;

/** Entrada da trilha estrita do assento, exposta apenas ao Pároco e ao Bispo Diocesano. */
public record HistoricoResponse(
        UUID id, String acao, String ator, String perfil, String enderecoIp, String valoresAnteriores,
        String valoresPosteriores, Instant ocorridoEmUtc) {

    public static HistoricoResponse de(HistoricoEntrada h) {
        return new HistoricoResponse(h.id(), h.acao(), h.ator(), h.perfil(), h.enderecoIp(), h.valoresAnteriores(),
                h.valoresPosteriores(), h.ocorridoEmUtc());
    }
}
