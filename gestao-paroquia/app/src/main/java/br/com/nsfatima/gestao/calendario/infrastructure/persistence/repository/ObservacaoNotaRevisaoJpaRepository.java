package br.com.nsfatima.gestao.calendario.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.calendario.infrastructure.persistence.entity.ObservacaoNotaRevisaoEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ObservacaoNotaRevisaoJpaRepository extends JpaRepository<ObservacaoNotaRevisaoEntity, UUID> {

    List<ObservacaoNotaRevisaoEntity> findByObservacaoIdOrderByRevisadoEmUtcAscIdAsc(UUID observacaoId);
}
