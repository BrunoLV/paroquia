package br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.CpcDecisaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CpcDecisaoJpaRepository extends JpaRepository<CpcDecisaoEntity, UUID> {

    List<CpcDecisaoEntity> findByComunidadeIdOrderByDataReuniaoDesc(UUID comunidadeId);

    List<CpcDecisaoEntity> findByStatusHomologacaoOrderByCriadoEmUtcDesc(StatusHomologacao status);

    List<CpcDecisaoEntity> findAllByOrderByDataReuniaoDesc();
}
