package br.com.nsfatima.gestao.projeto.domain.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Porta de domínio para consultas agregadas sobre eventos vinculados a um {@code Projeto},
 * usada para compor indicadores de saúde/execução do projeto (ver
 * {@code ProjetoAgregacaoDomainService}) sem que o domínio dependa diretamente do repositório
 * JPA de eventos do contexto {@code calendario}.
 */
public interface ProvedorEventoProjeto {

    /**
     * Conta o total de eventos vinculados ao projeto informado.
     *
     * @param projetoId ID do projeto.
     * @return quantidade total de eventos vinculados.
     */
    long contarPorProjetoId(UUID projetoId);

    /**
     * Conta os eventos vinculados ao projeto que estão em um determinado status.
     *
     * @param projetoId ID do projeto.
     * @param status    status do evento (nome do {@code EventoStatusInput}).
     * @return quantidade de eventos vinculados no status informado.
     */
    long contarPorProjetoIdEStatus(UUID projetoId, String status);

    /**
     * Conta os eventos vinculados ao projeto cuja data de término é anterior à data informada,
     * usado para identificar eventos já realizados.
     *
     * @param projetoId ID do projeto.
     * @param data      data de corte (exclusiva).
     * @return quantidade de eventos com término anterior à data informada.
     */
    long contarPorProjetoIdEFimUtcMenorQue(UUID projetoId, Instant data);

    /**
     * Conta os eventos vinculados ao projeto cuja data de término é igual ou posterior à data
     * informada, usado para identificar eventos ainda pendentes.
     *
     * @param projetoId ID do projeto.
     * @param data      data de corte (inclusiva).
     * @return quantidade de eventos com término igual ou posterior à data informada.
     */
    long contarPorProjetoIdEFimUtcMaiorOuIgualA(UUID projetoId, Instant data);

    /**
     * Lista os nomes distintos das organizações (pastorais/movimentos) envolvidas nos eventos
     * vinculados ao projeto, usado para montar o mapa de colaboração do projeto.
     *
     * @param projetoId ID do projeto.
     * @return lista de nomes de organizações envolvidas, sem duplicatas.
     */
    List<String> buscarNomesOrganizacoesEnvolvidas(UUID projetoId);
}
