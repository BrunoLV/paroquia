package br.com.nsfatima.gestao.sacramento.infrastructure.persistence.repository;

import br.com.nsfatima.gestao.sacramento.domain.model.TipoSacramento;
import br.com.nsfatima.gestao.sacramento.domain.model.Visibilidade;
import br.com.nsfatima.gestao.sacramento.infrastructure.persistence.entity.AssentoSacramentalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

/** Repositório Spring Data do assento sacramental. */
public interface AssentoSacramentalJpaRepository extends JpaRepository<AssentoSacramentalEntity, UUID> {

    @Query("""
            SELECT a FROM AssentoSacramentalEntity a
            WHERE (:tipo IS NULL OR a.tipo = :tipo) AND a.visibilidade IN :visibilidades
            ORDER BY a.dataLavratura DESC, a.criadoEmUtc DESC
            """)
    List<AssentoSacramentalEntity> listar(@Param("tipo") TipoSacramento tipo,
                                          @Param("visibilidades") Collection<Visibilidade> visibilidades);

    @Query("""
            SELECT COUNT(a) > 0 FROM AssentoSacramentalEntity a
            WHERE a.tipo = :tipo AND a.livro = :livro AND a.folha = :folha AND a.termo = :termo
              AND a.status <> 'CANCELADO' AND (:ignorarId IS NULL OR a.id <> :ignorarId)
            """)
    boolean existeReferencia(@Param("tipo") TipoSacramento tipo, @Param("livro") String livro,
                             @Param("folha") String folha, @Param("termo") String termo,
                             @Param("ignorarId") UUID ignorarId);

    @Query("""
            SELECT DISTINCT a FROM AssentoSacramentalEntity a JOIN a.participantes p
            WHERE (p.fielId = :pessoaId OR p.clerigoId = :pessoaId) AND a.visibilidade IN :visibilidades
            ORDER BY a.dataLavratura DESC
            """)
    List<AssentoSacramentalEntity> buscarPorVinculo(@Param("pessoaId") UUID pessoaId,
                                                   @Param("visibilidades") Collection<Visibilidade> visibilidades);

    @Query("""
            SELECT DISTINCT a FROM AssentoSacramentalEntity a JOIN a.participantes p
            WHERE p.vinculoPendente = true AND a.status <> 'CANCELADO' AND a.visibilidade IN :visibilidades
            ORDER BY a.dataLavratura DESC
            """)
    List<AssentoSacramentalEntity> listarComVinculoPendente(
            @Param("visibilidades") Collection<Visibilidade> visibilidades);
}
