package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório de persistência de {@link ObservacaoEventoEntity}, responsável pelas observações
 * (comentários/anotações) registradas em um evento, incluindo o suporte a remoção lógica
 * (soft delete via flag {@code removida}).
 */
public interface ObservacaoEventoJpaRepository extends JpaRepository<ObservacaoEventoEntity, UUID> {

    /**
     * Busca todas as observações de um evento, incluindo as removidas logicamente.
     *
     * @param eventoId identificador do evento
     * @return todas as observações associadas ao evento informado
     */
    List<ObservacaoEventoEntity> findByEventoId(UUID eventoId);

    /**
     * Busca as observações ativas (não removidas) de um evento, ordenadas cronologicamente pela
     * criação.
     *
     * @param eventoId identificador do evento
     * @return observações ativas do evento, ordenadas por criação e id ascendentes
     */
    List<ObservacaoEventoEntity> findByEventoIdAndRemovidaFalseOrderByCriadoEmUtcAscIdAsc(UUID eventoId);

    /**
     * Busca as observações ativas (não removidas) de um evento feitas por um usuário específico,
     * ordenadas cronologicamente pela criação.
     *
     * @param eventoId identificador do evento
     * @param usuarioId identificador do usuário autor das observações
     * @return observações ativas do usuário no evento informado, ordenadas por criação e id ascendentes
     */
    List<ObservacaoEventoEntity> findByEventoIdAndUsuarioIdAndRemovidaFalseOrderByCriadoEmUtcAscIdAsc(
            UUID eventoId,
            UUID usuarioId);

    /**
     * Busca as observações removidas logicamente de um evento, ordenadas cronologicamente pela
     * criação, usado para fins de auditoria/histórico.
     *
     * @param eventoId identificador do evento
     * @return observações removidas do evento informado, ordenadas por criação e id ascendentes
     */
    List<ObservacaoEventoEntity> findByEventoIdAndRemovidaTrueOrderByCriadoEmUtcAscIdAsc(UUID eventoId);

    /**
     * Busca uma observação específica garantindo que ela pertença ao evento informado, evitando
     * acesso cruzado a observações de outros eventos.
     *
     * @param id identificador da observação
     * @param eventoId identificador do evento ao qual a observação deve pertencer
     * @return a observação correspondente, ou {@link Optional#empty()} caso não exista ou não
     *     pertença ao evento informado
     */
    Optional<ObservacaoEventoEntity> findByIdAndEventoId(UUID id, UUID eventoId);
}
