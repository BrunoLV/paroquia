package br.com.nsfatima.gestao.clero.api.v1.dto;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.domain.model.GrauOrdem;
import java.time.LocalDate;
import java.util.UUID;

/** Resposta completa protegida para gestores do Clero. */
public record ClerigoResponse(
        UUID clerigoId, String nomeCivil, String nomeEclesiastico, LocalDate dataNascimento, String paisOrigem,
        String cpfFormatado, String rg, String documentoEstrangeiro, String email, String celularFormatado,
        boolean isWhatsapp, String telefoneFixoFormatado, String endereco, GrauOrdem grauOrdem,
        String dioceseIncardinacao, LocalDate dataOrdenacaoDiaconal, LocalDate dataOrdenacaoPresbiteral,
        LocalDate dataOrdenacaoEpiscopal, FuncaoEclesiastica funcaoAtual, boolean isAtivo) { }
