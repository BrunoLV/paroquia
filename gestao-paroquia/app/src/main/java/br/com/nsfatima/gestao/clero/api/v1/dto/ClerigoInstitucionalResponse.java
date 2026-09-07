package br.com.nsfatima.gestao.clero.api.v1.dto;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.domain.model.GrauOrdem;
import java.util.UUID;

/** Projeção institucional sem PII, própria para usuários autenticados sem gestão do Clero. */
public record ClerigoInstitucionalResponse(
        UUID clerigoId, String nomeEclesiastico, GrauOrdem grauOrdem, FuncaoEclesiastica funcaoAtual,
        String paroquia) { }
