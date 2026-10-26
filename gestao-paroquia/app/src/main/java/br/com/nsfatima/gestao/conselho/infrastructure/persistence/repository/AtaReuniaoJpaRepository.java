package br.com.nsfatima.gestao.conselho.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.conselho.domain.model.StatusAta;
import br.com.nsfatima.gestao.conselho.domain.model.TipoReuniao;
import br.com.nsfatima.gestao.conselho.infrastructure.persistence.entity.AtaReuniaoEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.UUID;

/** Repositório Spring Data da ata de reunião. */
public interface AtaReuniaoJpaRepository extends JpaRepository<AtaReuniaoEntity, UUID> {

    @Query("""
            SELECT a FROM AtaReuniaoEntity a
            WHERE (:irrestrito = TRUE OR a.organizacaoConvocanteId IN :permitidas
                   OR a.organizacaoAssociadaId IN :permitidas)
              AND (:organizacaoId IS NULL OR a.organizacaoConvocanteId = :organizacaoId)
              AND (:tipo IS NULL OR a.tipo = :tipo)
              AND (:qualquerStatus = TRUE OR a.status IN :statuses)
              AND (:de IS NULL OR a.dataReuniao >= :de)
              AND (:ate IS NULL OR a.dataReuniao <= :ate)
            """)
    Page<AtaReuniaoEntity> listar(@Param("irrestrito") boolean irrestrito,
                                  @Param("permitidas") Collection<UUID> permitidas,
                                  @Param("organizacaoId") UUID organizacaoId,
                                  @Param("tipo") TipoReuniao tipo,
                                  @Param("qualquerStatus") boolean qualquerStatus,
                                  @Param("statuses") Collection<StatusAta> statuses,
                                  @Param("de") LocalDate de,
                                  @Param("ate") LocalDate ate,
                                  Pageable pageable);

    @Query("""
            SELECT COALESCE(MAX(a.numero), 0) FROM AtaReuniaoEntity a
            WHERE a.organizacaoConvocanteId = :organizacaoId AND a.ano = :ano
            """)
    int maiorNumero(@Param("organizacaoId") UUID organizacaoId, @Param("ano") int ano);
}
