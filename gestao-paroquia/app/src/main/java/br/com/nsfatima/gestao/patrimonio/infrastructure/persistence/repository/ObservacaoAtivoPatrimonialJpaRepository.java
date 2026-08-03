package br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.patrimonio.infrastructure.persistence.entity.ObservacaoAtivoPatrimonialEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Acesso a persistência das notas de observação de ativos patrimoniais, com consulta que já
 * exclui notas removidas logicamente e ordena pela ordem de criação.
 */
@Repository
public interface ObservacaoAtivoPatrimonialJpaRepository extends JpaRepository<ObservacaoAtivoPatrimonialEntity, UUID> {

    List<ObservacaoAtivoPatrimonialEntity> findByAtivoPatrimonialIdAndRemovidaFalseOrderByCriadoEmUtcAscIdAsc(
            UUID ativoPatrimonialId);
}
