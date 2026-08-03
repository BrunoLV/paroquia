package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity;
import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoEnvolvidoEntity.Key;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório de persistência de {@link EventoEnvolvidoEntity}, responsável pelo vínculo entre
 * um evento e as organizações nele envolvidas (além da organização responsável principal).
 *
 * <p>Faz parte da segregação por responsabilidade do agregado Evento (ver Javadoc de
 * {@link EventoJpaRepository} para o racional completo): esta interface concentra apenas as
 * consultas sobre a lista de participantes/envolvidos de um evento, independente das
 * responsabilidades de consulta de calendário, estatística ou validação.
 */
public interface EventoEnvolvidoJpaRepository extends JpaRepository<EventoEnvolvidoEntity, Key> {

    /**
     * Busca todas as organizações envolvidas em um determinado evento.
     *
     * @param eventoId identificador do evento
     * @return vínculos de envolvimento associados ao evento informado
     */
    List<EventoEnvolvidoEntity> findByEventoId(UUID eventoId);

    /**
     * Remove todos os vínculos de organizações envolvidas de um determinado evento, tipicamente
     * usado ao reescrever a lista de envolvidos em uma atualização do evento.
     *
     * @param eventoId identificador do evento cujos vínculos de envolvimento serão removidos
     */
    void deleteByEventoId(UUID eventoId);
}
