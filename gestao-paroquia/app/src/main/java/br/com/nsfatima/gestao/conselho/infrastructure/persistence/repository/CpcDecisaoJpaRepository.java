package br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.conselho.domain.model.StatusHomologacao;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.CpcDecisaoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Acesso a persistência das atas/decisões de conselho (CPC/CPP), com métodos de consulta
 * dedicados aos filtros usados por {@link br.com.nsfatima.gestao.conselho.application.usecase.ListarCpcDecisoesUseCase}
 * (por comunidade, por status de homologação, ou todas).
 */
@Repository
public interface CpcDecisaoJpaRepository extends JpaRepository<CpcDecisaoEntity, UUID> {

    /**
     * Lista as decisões de uma comunidade específica.
     *
     * @param comunidadeId comunidade a filtrar
     * @return decisões da comunidade, ordenadas da reunião mais recente para a mais antiga
     */
    List<CpcDecisaoEntity> findByComunidadeIdOrderByDataReuniaoDesc(UUID comunidadeId);

    /**
     * Lista as decisões por status de homologação, independentemente da comunidade.
     *
     * @param status status de homologação a filtrar
     * @return decisões com o status informado, ordenadas da mais recentemente criada para a mais antiga
     */
    List<CpcDecisaoEntity> findByStatusHomologacaoOrderByCriadoEmUtcDesc(StatusHomologacao status);

    /**
     * Lista todas as decisões de conselho, sem filtro.
     *
     * @return todas as decisões, ordenadas da reunião mais recente para a mais antiga
     */
    List<CpcDecisaoEntity> findAllByOrderByDataReuniaoDesc();
}
