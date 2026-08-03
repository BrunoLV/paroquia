package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoIdempotencyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositório de persistência de {@link EventoIdempotencyEntity}, responsável por registrar e
 * consultar chaves de idempotência associadas a requisições de criação/alteração de eventos, de
 * forma a evitar o processamento duplicado da mesma requisição.
 *
 * <p>Faz parte da segregação por responsabilidade do agregado Evento (ver Javadoc de
 * {@link EventoJpaRepository} para o racional completo): esta interface é independente das
 * demais, pois trata de uma preocupação puramente técnica (idempotência), sem sobreposição com
 * consultas de calendário, estatística ou validação de regra de negócio.
 */
public interface EventoIdempotencyJpaRepository extends JpaRepository<EventoIdempotencyEntity, String> {
}
