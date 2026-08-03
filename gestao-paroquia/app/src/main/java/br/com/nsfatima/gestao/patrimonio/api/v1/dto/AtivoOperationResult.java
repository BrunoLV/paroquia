package br.com.nsfatima.gestao.patrimonio.api.v1.dto;

import org.springframework.http.HttpStatus;

/**
 * Resultado da tentativa de alocar um ativo a um evento, que pode terminar de duas formas
 * distintas: {@link Success} quando a alocação é confirmada de imediato (mesma organização), ou
 * {@link Pendente} quando vira um pedido de empréstimo aguardando aprovação — cada variante carrega
 * seu próprio status HTTP para o controller repassar na resposta.
 */
public sealed interface AtivoOperationResult {

    HttpStatus status();

    Object body();

    /** Alocação aplicada com sucesso ao ativo. */
    record Success(Object response, HttpStatus status) implements AtivoOperationResult {
        @Override
        public Object body() {
            return response;
        }
    }

    /** Alocação não aplicada ainda: virou uma solicitação de aprovação de empréstimo. */
    record Pendente(Object response, HttpStatus status) implements AtivoOperationResult {
        @Override
        public Object body() {
            return response;
        }
    }
}
