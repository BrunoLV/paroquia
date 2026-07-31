package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.domain.model.AtivoStatus;
import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.AtivoPatrimonialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AtivoPatrimonialJpaRepository extends JpaRepository<AtivoPatrimonialEntity, UUID> {

    List<AtivoPatrimonialEntity> findByOrganizacaoIdOrderByNomeAsc(UUID organizacaoId);

    List<AtivoPatrimonialEntity> findByStatus(AtivoStatus status);

    long countByStatus(AtivoStatus status);
}
