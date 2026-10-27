package br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.RelacionamentoFamiliarEntity;
import br.com.nsfatima.gestao.clero.domain.model.TipoPessoaFamiliar;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RelacionamentoFamiliarJpaRepository extends JpaRepository<RelacionamentoFamiliarEntity, UUID> {
    List<RelacionamentoFamiliarEntity> findByPessoaOrigemIdAndTipoPessoaOrigemAndDeletedAtIsNull(
            UUID pessoaId, TipoPessoaFamiliar tipoPessoa);
    List<RelacionamentoFamiliarEntity> findByParIdAndDeletedAtIsNull(UUID parId);

    /** Arestas vigentes em que a pessoa figura como origem ou destino, nas duas direções do vínculo. */
    @Query("SELECT r FROM RelacionamentoFamiliarEntity r WHERE r.deletedAt IS NULL "
            + "AND (r.pessoaOrigemId = :pessoaId OR r.pessoaDestinoId = :pessoaId)")
    List<RelacionamentoFamiliarEntity> findVigentesDaPessoa(@Param("pessoaId") UUID pessoaId);
}
