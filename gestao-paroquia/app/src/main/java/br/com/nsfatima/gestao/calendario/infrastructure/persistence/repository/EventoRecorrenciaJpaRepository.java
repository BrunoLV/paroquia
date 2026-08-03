package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoRecorrenciaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Repositório de persistência de {@link EventoRecorrenciaEntity}, responsável pelo CRUD das
 * regras de recorrência que originam séries de eventos repetidos (ex.: missas semanais).
 *
 * <p>Faz parte da segregação por responsabilidade do agregado Evento (ver Javadoc de
 * {@link EventoJpaRepository} para o racional completo): esta interface trata exclusivamente do
 * agregado de recorrência (a "regra" da série), enquanto as ocorrências individuais de evento
 * geradas por essa regra são consultadas via {@link EventoConsultaJpaRepository#findByRecorrenciaId}.
 */
public interface EventoRecorrenciaJpaRepository extends JpaRepository<EventoRecorrenciaEntity, UUID> {
}
