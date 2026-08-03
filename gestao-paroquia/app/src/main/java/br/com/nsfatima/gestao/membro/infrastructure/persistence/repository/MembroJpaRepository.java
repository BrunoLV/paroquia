package br.com.nsfatima.gestao.membro.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.membro.infrastructure.persistence.entity.MembroEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Acesso a persistência dos vínculos usuário-organização ({@code MembroEntity}). Concentra as buscas usadas para
 * listagem paginada com filtros e para as checagens de vínculo ativo único que impedem duplicidade
 * usuário+organização (ver {@code CreateMembroUseCase} e {@code UpdateMembroUseCase}).
 */
@Repository
public interface MembroJpaRepository extends JpaRepository<MembroEntity, UUID> {

    /**
     * Lista vínculos aplicando filtros opcionais (qualquer parâmetro {@code null} é ignorado na cláusula
     * {@code WHERE}), usado pela listagem administrativa paginada de membros.
     *
     * @param organizacaoId filtro por organização, ou {@code null} para não filtrar
     * @param usuarioId     filtro por usuário, ou {@code null} para não filtrar
     * @param ativo         filtro por situação do vínculo, ou {@code null} para não filtrar
     * @param pageable      paginação/ordenação solicitada
     * @return página de vínculos que atendem aos filtros informados
     */
    @Query("""
            SELECT m FROM MembroEntity m
            WHERE (:organizacaoId IS NULL OR m.organizacaoId = :organizacaoId)
            AND (:usuarioId IS NULL OR m.usuarioId = :usuarioId)
            AND (:ativo IS NULL OR m.ativo = :ativo)
            """)
    Page<MembroEntity> findByFiltros(
            @Param("organizacaoId") UUID organizacaoId,
            @Param("usuarioId") UUID usuarioId,
            @Param("ativo") Boolean ativo,
            Pageable pageable);

    Optional<MembroEntity> findByUsuarioIdAndOrganizacaoId(UUID usuarioId, UUID organizacaoId);

    /**
     * Busca o vínculo ativo (se houver) entre um usuário e uma organização — usado para impedir que o mesmo
     * usuário tenha mais de um vínculo ativo simultâneo com a mesma organização.
     */
    Optional<MembroEntity> findByUsuarioIdAndOrganizacaoIdAndAtivoTrue(UUID usuarioId, UUID organizacaoId);

    List<MembroEntity> findByUsuarioId(UUID usuarioId);

    List<MembroEntity> findByOrganizacaoId(UUID organizacaoId);
}
