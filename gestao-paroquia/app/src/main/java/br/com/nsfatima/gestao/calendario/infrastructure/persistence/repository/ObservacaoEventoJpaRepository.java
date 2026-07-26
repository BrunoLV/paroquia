package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoEventoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ObservacaoEventoJpaRepository extends JpaRepository<ObservacaoEventoEntity, UUID> {

    List<ObservacaoEventoEntity> findByEventoId(UUID eventoId);

    List<ObservacaoEventoEntity> findByEventoIdAndRemovidaFalseOrderByCriadoEmUtcAscIdAsc(UUID eventoId);

    List<ObservacaoEventoEntity> findByEventoIdAndUsuarioIdAndRemovidaFalseOrderByCriadoEmUtcAscIdAsc(
            UUID eventoId,
            UUID usuarioId);

    List<ObservacaoEventoEntity> findByEventoIdAndRemovidaTrueOrderByCriadoEmUtcAscIdAsc(UUID eventoId);

    Optional<ObservacaoEventoEntity> findByIdAndEventoId(UUID id, UUID eventoId);
}
