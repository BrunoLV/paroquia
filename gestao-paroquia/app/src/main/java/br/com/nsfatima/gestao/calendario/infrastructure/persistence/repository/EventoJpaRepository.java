package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repositório Spring Data JPA agregador de {@link EventoEntity}, ponto de acesso "completo" ao
 * agregado Evento para os consumidores que precisam de toda a superfície de consulta.
 *
 * <p>{@code EventoEntity} é um dos maiores "god nodes" do sistema (evento é o agregado mais
 * conectado do contexto de calendário). Para evitar um único repositório inchado com dezenas de
 * métodos não relacionados, as consultas foram deliberadamente segregadas por responsabilidade
 * (Interface Segregation Principle) em interfaces {@code @NoRepositoryBean} dedicadas, cada uma
 * agrupando um subconjunto coeso de consultas sobre Evento:
 * <ul>
 *   <li>{@link EventoConsultaJpaRepository} — consultas de leitura do calendário (filtros,
 *       período, sobreposição de eventos, organizações envolvidas em um projeto);</li>
 *   <li>{@link EventoEstatisticaJpaRepository} — consultas agregadas/estatísticas (contagens
 *       por organização, status e período, usadas em métricas e indicadores);</li>
 *   <li>{@link EventoValidacaoJpaRepository} — consultas de existência usadas em regras de
 *       negócio de validação (conflito de horário, vínculo a local/organização/recorrência);</li>
 *   <li>{@link EventoEnvolvidoJpaRepository}, {@link EventoRecorrenciaJpaRepository} e
 *       {@link EventoIdempotencyJpaRepository} — repositórios irmãos, independentes, para os
 *       agregados/relacionamentos de participantes, séries recorrentes e chaves de
 *       idempotência, respectivamente.</li>
 * </ul>
 * Essa segregação permite que cada caso de uso dependa apenas da superfície de consulta que
 * realmente utiliza, em vez de um único {@code EventoRepository} monolítico. Este repositório
 * combina as interfaces de consulta, estatística e validação com o CRUD padrão do Spring Data
 * para os consumidores que precisam da visão consolidada do agregado Evento.
 */
@Repository
public interface EventoJpaRepository extends JpaRepository<EventoEntity, UUID>,
        EventoConsultaJpaRepository, EventoEstatisticaJpaRepository, EventoValidacaoJpaRepository {
}
