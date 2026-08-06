package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Acesso a persistência do inventário de ativos patrimoniais, incluindo as consultas usadas para
 * listagem por organização e para o cálculo de métricas de ociosidade por status.
 */
@Repository
public interface AtivoPatrimonialJpaRepository extends JpaRepository<AtivoPatrimonialEntity, UUID> {

    List<AtivoPatrimonialEntity> findByGrupoParoquialIdOrderByNomeAsc(UUID grupoParoquialId);

    List<AtivoPatrimonialEntity> findByStatus(AtivoStatus status);

    long countByStatus(AtivoStatus status);
}
