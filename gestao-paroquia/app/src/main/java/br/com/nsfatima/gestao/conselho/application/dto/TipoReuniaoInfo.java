package br.com.nsfatima.gestao.conselho.application.dto;

import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.domain.policy.RegrasTipoReuniao;

/** Tipo de reunião e as regras que a interface precisa para montar o formulário (presença, votação, quórum). */
public record TipoReuniaoInfo(TipoReuniao tipo, RegrasTipoReuniao regras) {
}
