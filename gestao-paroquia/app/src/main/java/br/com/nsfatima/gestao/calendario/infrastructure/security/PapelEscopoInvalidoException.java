package br.com.nsfatima.gestao.calendario.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;

/**
 * Exceção de acesso negado lançada quando o papel organizacional do ator autenticado é incompatível
 * com o tipo de organização do seu vínculo (ex.: um papel só válido para {@code CLERO} sendo usado
 * em uma organização do tipo {@code PASTORAL}). É lançada por
 * {@link EventoActorContextResolver#resolveRequired()} ao não encontrar, entre os vínculos do
 * usuário, nenhum cuja combinação papel/tipo de organização seja permitida pela
 * {@code AuthorizationPolicy}, e é tratada por {@link JsonAuthenticationOutcomeProcessor} para
 * responder com o código de erro {@code ROLE_SCOPE_INVALID}.
 */
public class PapelEscopoInvalidoException extends AccessDeniedException {

    /**
     * Cria a exceção com a mensagem descrevendo a incompatibilidade entre papel e escopo de
     * organização.
     *
     * @param message mensagem explicando por que a combinação de papel e escopo é inválida
     */
    public PapelEscopoInvalidoException(String message) {
        super(message);
    }
}
