package br.com.nsfatima.gestao.clero.api.v1.dto;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.domain.model.GrauOrdem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/** Contrato administrativo do cadastro civil e canônico do clérigo. */
public record ClerigoRequest(
        @NotBlank String nomeCivil, @NotBlank String nomeEclesiastico, @NotNull LocalDate dataNascimento,
        @NotBlank String paisOrigem, String cpf, String rg, String documentoEstrangeiro,
        @NotBlank String email, @NotBlank String celular, boolean isWhatsapp, String telefoneFixo, String endereco,
        @NotNull GrauOrdem grauOrdem, @NotBlank String dioceseIncardinacao,
        LocalDate dataOrdenacaoDiaconal, LocalDate dataOrdenacaoPresbiteral, LocalDate dataOrdenacaoEpiscopal,
        FuncaoEclesiastica funcaoAtual) { }
