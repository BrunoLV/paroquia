package br.com.nsfatima.gestao.sacramento.application.port.out;

import java.util.Optional;
import java.util.UUID;

/**
 * Porta de leitura dos cadastros de fiel e clérigo usados no vínculo tardio. Devolve apenas o nome
 * civil, suficiente para preencher o snapshot textual e validar a existência do cadastro; nenhum
 * outro dado pessoal atravessa a fronteira do contexto.
 */
public interface ConsultaPessoaPort {

    /**
     * Busca o nome civil de um fiel.
     *
     * @param fielId identificador do fiel
     * @return nome civil, ou vazio se o cadastro não existe
     */
    Optional<String> nomeDoFiel(UUID fielId);

    /**
     * Busca o nome civil de um clérigo.
     *
     * @param clerigoId identificador do clérigo
     * @return nome civil, ou vazio se o cadastro não existe
     */
    Optional<String> nomeDoClerigo(UUID clerigoId);
}
