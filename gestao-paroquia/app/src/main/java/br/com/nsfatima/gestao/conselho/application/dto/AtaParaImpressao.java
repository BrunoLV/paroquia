package br.com.nsfatima.gestao.conselho.application.dto;

import br.com.nsfatima.gestao.conselho.domain.model.AtaReuniao;

import java.util.Map;
import java.util.UUID;

/**
 * Ata e o que é preciso resolver fora dela para imprimi-la: nomes de pessoas e organizações, que a ata não
 * persiste, e a identificação da ata original quando esta é retificadora.
 */
public record AtaParaImpressao(
        AtaReuniao ata,
        Map<UUID, String> nomesFieis,
        String organizacao,
        String organizacaoAssociada,
        String identificacaoAtaRetificada) {

    public AtaParaImpressao {
        nomesFieis = Map.copyOf(nomesFieis);
    }
}
