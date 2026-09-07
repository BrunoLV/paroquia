package br.com.nsfatima.gestao.clero.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.clero.domain.model.FuncaoEclesiastica;
import br.com.nsfatima.gestao.clero.infrastructure.persistence.entity.EncargoClericalEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Consultas dos encargos clericais atuais e históricos. */
public interface EncargoClericalJpaRepository extends JpaRepository<EncargoClericalEntity, UUID> {
    List<EncargoClericalEntity> findByClerigoIdOrderByCriadoEmUtcDesc(UUID clerigoId);
    List<EncargoClericalEntity> findByFuncaoAndAtualTrue(FuncaoEclesiastica funcao);
    List<EncargoClericalEntity> findByClerigoIdAndAtualTrue(UUID clerigoId);
}
