package br.com.nsfatima.gestao.calendario.domain.policy;

import java.util.UUID;

/** Snapshot imutável da autoria e mutabilidade de uma observação já carregada. */
public record ObservacaoRegistrada(String tipo, boolean removida, UUID autorId) {
}
