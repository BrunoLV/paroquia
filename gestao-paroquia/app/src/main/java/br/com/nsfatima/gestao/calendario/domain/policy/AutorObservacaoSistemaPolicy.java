package br.com.nsfatima.gestao.calendario.domain.policy;

import java.util.UUID;

/** Resolve a autoria técnica para fatos de calendário disparados sem um ator humano. */
@FunctionalInterface
public interface AutorObservacaoSistemaPolicy {

    UUID resolve(UUID atorId);
}
