package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.EventoLocalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repositório JPA para persistência e consulta das associações de locais de eventos
 * ({@link EventoLocalEntity}).
 */
@Repository
public interface EventoLocalJpaRepository extends JpaRepository<EventoLocalEntity, EventoLocalEntity.Key> {

    /**
     * Busca todas as associações de local vinculadas a um determinado evento.
     *
     * @param eventoId identificador do evento
     * @return lista de entidades de associação de local
     */
    List<EventoLocalEntity> findByEventoId(UUID eventoId);

    /**
     * Remove todas as associações de local de um evento (usado na sincronização/atualização).
     *
     * @param eventoId identificador do evento
     */
    void deleteByEventoId(UUID eventoId);

    /**
     * Verifica se existe alguma associação vinculada a um local específico.
     *
     * @param localId identificador do local
     * @return {@code true} se houver vínculo
     */
    boolean existsByLocalId(UUID localId);
}
