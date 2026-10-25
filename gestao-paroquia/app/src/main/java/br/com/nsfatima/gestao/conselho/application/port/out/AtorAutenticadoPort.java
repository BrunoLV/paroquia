package br.com.nsfatima.gestao.conselho.application.port.out;

import br.com.nsfatima.gestao.conselho.application.dto.AtorAutenticado;

/** Porta de saída que entrega o usuário autenticado da requisição corrente, sem expor o framework de segurança. */
public interface AtorAutenticadoPort {

    /**
     * Usuário autenticado da requisição.
     *
     * @return o ator, com papéis globais e vínculos organizacionais
     */
    AtorAutenticado atual();
}
