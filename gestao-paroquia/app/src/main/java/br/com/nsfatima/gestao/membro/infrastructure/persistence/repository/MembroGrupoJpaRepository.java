package br.com.nsfatima.gestao.membro.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroGrupoEntity;
import br.com.nsfatima.gestao.organizacao.domain.model.TipoGrupo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso a persistência dos vínculos {@code Fiel}-organização ({@link MembroGrupoEntity}), unificando o que
 * antes eram três tabelas/repositórios separados (membros, membros_grupoParoquial, membros_organizacoes).
 */
@Repository
public interface MembroGrupoJpaRepository extends JpaRepository<MembroGrupoEntity, UUID> {

    /**
     * Lista vínculos aplicando filtros opcionais (qualquer parâmetro {@code null} é ignorado na cláusula
     * {@code WHERE}), usado pela listagem administrativa paginada de membros.
     */
    @Query("""
            SELECT m FROM MembroGrupoEntity m
            WHERE (:grupoParoquialId IS NULL OR m.grupoParoquialId = :grupoParoquialId)
            AND (:fielId IS NULL OR m.fielId = :fielId)
            AND (:ativo IS NULL OR m.ativo = :ativo)
            """)
    Page<MembroGrupoEntity> findByFiltros(
            @Param("grupoParoquialId") UUID grupoParoquialId,
            @Param("fielId") UUID fielId,
            @Param("ativo") Boolean ativo,
            Pageable pageable);

    Optional<MembroGrupoEntity> findByFielIdAndGrupoParoquialIdAndAtivoTrue(UUID fielId, UUID grupoParoquialId);

    Optional<MembroGrupoEntity> findByFielIdAndGrupoParoquialIdAndPapelAndIgrejaVinculoId(
            UUID fielId, UUID grupoParoquialId, String papel, UUID igrejaVinculoId);

    Optional<MembroGrupoEntity> findByGrupoParoquialIdAndPapelAndIgrejaVinculoIdAndAtivoTrue(
            UUID grupoParoquialId, String papel, UUID igrejaVinculoId);

    Optional<MembroGrupoEntity> findByGrupoParoquialIdAndPapelAndIgrejaVinculoIdNullAndAtivoTrue(
            UUID grupoParoquialId, String papel);

    List<MembroGrupoEntity> findByFielId(UUID fielId);

    List<MembroGrupoEntity> findByFielIdAndAtivoTrue(UUID fielId);

    List<MembroGrupoEntity> findByGrupoParoquialIdAndAtivoTrue(UUID grupoParoquialId);

    List<MembroGrupoEntity> findByIgrejaVinculoIdAndAtivoTrue(UUID igrejaVinculoId);

    boolean existsByGrupoParoquialId(UUID grupoParoquialId);

    /**
     * Conta fiéis <em>distintos</em> com pelo menos um vínculo ativo em grupos dos tipos
     * informados, usado pelo indicador de engajamento pastoral do dashboard (um fiel em três
     * pastorais conta uma vez).
     *
     * @param tipos tipos de grupo considerados engajamento (tipicamente {@code PASTORAL},
     *     {@code MOVIMENTO})
     * @return quantidade de fiéis distintos engajados
     */
    @Query("""
            SELECT count(DISTINCT m.fielId) FROM MembroGrupoEntity m
            WHERE m.ativo = true AND m.tipoGrupo IN :tipos
            """)
    long contarFieisDistintosEngajados(@Param("tipos") Collection<TipoGrupo> tipos);

    /**
     * Conta vínculos ativos de um grupo, usado pelo índice de retenção de pastorais.
     *
     * @param grupoParoquialId identificador do grupo
     * @return quantidade de vínculos ativos
     */
    long countByGrupoParoquialIdAndAtivoTrue(UUID grupoParoquialId);
}
