package br.com.nsfatima.gestao.calendario.infrastructure.external;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository.EventoJpaRepository;
import br.com.nsfatima.gestao.projeto.domain.service.ProvedorEventoProjeto;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Adapta o {@link EventoJpaRepository} do contexto de calendário à porta
 * {@link ProvedorEventoProjeto} definida pelo contexto de projeto, permitindo que o contexto de
 * projeto consulte contagens e organizações envolvidas de seus eventos sem depender diretamente
 * das entidades ou repositórios internos de calendário — a comunicação cruza a fronteira entre
 * contextos apenas por IDs e tipos primitivos, preservando o isolamento entre bounded contexts.
 */
@Component
public class ProvedorEventoProjetoCalendario implements ProvedorEventoProjeto {

    private final EventoJpaRepository eventoRepository;

    public ProvedorEventoProjetoCalendario(EventoJpaRepository eventoRepository) {
        this.eventoRepository = eventoRepository;
    }

    /**
     * Conta o total de eventos vinculados a um projeto.
     *
     * @param projetoId identificador do projeto
     * @return quantidade total de eventos do projeto
     */
    @Override
    public long contarPorProjetoId(UUID projetoId) {
        return eventoRepository.countByProjetoId(projetoId);
    }

    /**
     * Conta os eventos de um projeto que estão em um determinado status.
     *
     * @param projetoId identificador do projeto
     * @param status status do evento, representado como {@code String} para não vazar o enum
     *     de calendário para o contexto de projeto
     * @return quantidade de eventos do projeto no status informado
     */
    @Override
    public long contarPorProjetoIdEStatus(UUID projetoId, String status) {
        return eventoRepository.countByProjetoIdAndStatus(projetoId, status);
    }

    /**
     * Conta os eventos de um projeto cujo término ocorre antes da data informada.
     *
     * @param projetoId identificador do projeto
     * @param data data de corte (exclusiva) usada para comparar o término dos eventos
     * @return quantidade de eventos do projeto encerrados antes da data informada
     */
    @Override
    public long contarPorProjetoIdEFimUtcMenorQue(UUID projetoId, Instant data) {
        return eventoRepository.countByProjetoIdAndFimUtcLessThan(projetoId, data);
    }

    /**
     * Conta os eventos de um projeto cujo término ocorre a partir da data informada (inclusive).
     *
     * @param projetoId identificador do projeto
     * @param data data de corte (inclusiva) usada para comparar o término dos eventos
     * @return quantidade de eventos do projeto ainda não encerrados na data informada
     */
    @Override
    public long contarPorProjetoIdEFimUtcMaiorOuIgualA(UUID projetoId, Instant data) {
        return eventoRepository.countByProjetoIdAndFimUtcGreaterThanEqual(projetoId, data);
    }

    /**
     * Conta os eventos não cancelados de um projeto que ficariam fora do período informado.
     *
     * @param projetoId identificador do projeto
     * @param inicio novo início do projeto
     * @param fim novo fim do projeto
     * @return quantidade de eventos do projeto fora do novo período
     */
    @Override
    public long contarEventosForaDoPeriodo(UUID projetoId, Instant inicio, Instant fim) {
        return eventoRepository.contarEventosVinculadosForaDoPeriodo(projetoId, inicio, fim);
    }

    /**
     * Lista os nomes das organizações envolvidas nos eventos de um projeto.
     *
     * @param projetoId identificador do projeto
     * @return nomes das organizações participantes dos eventos do projeto
     */
    @Override
    public List<String> buscarNomesOrganizacoesEnvolvidas(UUID projetoId) {
        return eventoRepository.findInvolvedOrganizationNames(projetoId);
    }
}
