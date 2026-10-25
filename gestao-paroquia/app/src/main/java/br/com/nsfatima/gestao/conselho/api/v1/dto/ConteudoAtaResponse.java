package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.AprovacaoAtaAnterior;
import br.com.nsfatima.gestao.conselho.domain.model.CaraterReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ModalidadeReuniao;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Conteúdo da ata, com nomes de pessoas resolvidos na leitura. */
public record ConteudoAtaResponse(
        String titulo,
        LocalDate dataReuniao,
        String local,
        CaraterReuniao carater,
        ModalidadeReuniao modalidade,
        List<PresencaResponse> presencas,
        Integer totalParticipantes,
        UUID ataAnteriorId,
        AprovacaoAtaAnterior aprovacaoAtaAnterior,
        String ressalvasAtaAnterior,
        List<ItemPautaResponse> itens) {
}
