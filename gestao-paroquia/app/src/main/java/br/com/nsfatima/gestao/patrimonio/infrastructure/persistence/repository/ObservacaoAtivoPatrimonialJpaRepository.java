package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.ObservacaoAtivoPatrimonialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ObservacaoAtivoPatrimonialJpaRepository extends JpaRepository<ObservacaoAtivoPatrimonialEntity, UUID> {

    List<ObservacaoAtivoPatrimonialEntity> findByAtivoPatrimonialIdAndRemovidaFalseOrderByCriadoEmUtcAscIdAsc(
            UUID ativoPatrimonialId);
}
