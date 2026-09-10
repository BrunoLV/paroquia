package br.com.nsfatima.gestao.projeto.domain.policy;

import br.com.nsfatima.gestao.calendario.domain.type.RegraRecorrencia;

import java.time.Instant;

/** Define a regra pura de admissibilidade de eventos no período de um projeto já carregado. */
public interface ProjetoVincularPolicy {

    void validarEvento(ProjetoVinculo projeto, Instant inicioEvento, Instant fimEvento);

    void validarRecorrencia(ProjetoVinculo projeto, Instant inicioEvento, Instant fimEvento, RegraRecorrencia regra);
}
