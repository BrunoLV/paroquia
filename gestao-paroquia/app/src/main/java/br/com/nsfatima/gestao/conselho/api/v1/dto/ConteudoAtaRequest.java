package br.com.nsfatima.gestao.conselho.api.v1.dto;

import br.com.nsfatima.gestao.conselho.domain.model.CaraterReuniao;
import br.com.nsfatima.gestao.conselho.domain.model.ModalidadeReuniao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/** Conteúdo editável da ata, enviado por inteiro a cada gravação do rascunho. */
public record ConteudoAtaRequest(
        @NotBlank @Size(max = 160) String titulo,
        @NotNull LocalDate dataReuniao,
        @NotBlank @Size(max = 200) String local,
        @NotNull CaraterReuniao carater,
        @NotNull ModalidadeReuniao modalidade,
        @Valid PresencaRequest presenca,
        @Valid AtaAnteriorRequest ataAnterior,
        List<@Valid ItemPautaRequest> itens) {
}
