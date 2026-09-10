package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Acesso a persistência do inventário de ativos patrimoniais. Listagens desconsideram bens com
 * exclusão lógica; a busca por id inclui os excluídos (a distinção "não existe" x "foi excluído" é
 * responsabilidade do caso de uso).
 */
@Repository
public interface AtivoPatrimonialJpaRepository extends JpaRepository<AtivoPatrimonialEntity, UUID> {

    List<AtivoPatrimonialEntity> findByGrupoParoquialIdAndDeletedAtIsNullOrderByNomeAsc(UUID grupoParoquialId);

    List<AtivoPatrimonialEntity> findByDeletedAtIsNullOrderByNomeAsc();

    @Query("""
            SELECT a FROM AtivoPatrimonialEntity a
            WHERE a.deletedAt IS NULL AND a.status <> :emManutencao
            """)
    List<AtivoPatrimonialEntity> findSincronizaveisDeReserva(@Param("emManutencao") AtivoStatus emManutencao);

    long countByStatusAndDeletedAtIsNull(AtivoStatus status);
}
