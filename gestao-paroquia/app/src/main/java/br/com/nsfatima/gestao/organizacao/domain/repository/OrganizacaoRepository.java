package br.com.nsfatima.gestao.organizacao.domain.repository;

import br.com.nsfatima.gestao.organizacao.domain.model.Organizacao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Porta de persistência (Ports &amp; Adapters) para a raiz de agregado {@link Organizacao}, isolando o
 * domínio dos detalhes de JPA. Implementada por {@code OrganizacaoRepositoryImpl}, que resolve nomes de
 * coordenador/vice-coordenador a partir dos vínculos de membro ativos.
 */
public interface OrganizacaoRepository {
    /**
     * Persiste (insere ou atualiza) a organização informada.
     *
     * @param organizacao organização a ser salva
     */
    void save(Organizacao organizacao);

    /**
     * Busca uma organização pelo seu identificador.
     *
     * @param id identificador da organização
     * @return a organização encontrada, ou {@link Optional#empty()} se não existir
     */
    Optional<Organizacao> findById(UUID id);

    /**
     * Lista todas as organizações cadastradas, ativas ou inativas.
     *
     * @return lista de todas as organizações
     */
    List<Organizacao> findAll();

    /**
     * Verifica se a organização possui dependências ativas (membros vinculados ou eventos associados) que
     * impeçam sua exclusão segura.
     *
     * @param id identificador da organização
     * @return {@code true} se houver ao menos uma dependência ativa
     */
    boolean hasDependencies(UUID id);

    /**
     * Remove definitivamente a organização identificada. Quem chama deve garantir previamente, via
     * {@link #hasDependencies(UUID)}, que não há dependências ativas.
     *
     * @param id identificador da organização a ser removida
     */
    void delete(UUID id);
}
