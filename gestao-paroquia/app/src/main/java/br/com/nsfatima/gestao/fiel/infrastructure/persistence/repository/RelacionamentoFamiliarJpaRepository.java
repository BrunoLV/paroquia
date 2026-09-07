package br.com.nsfatima.gestao.fiel.infrastructure.persistence.repository;
import br.com.nsfatima.gestao.fiel.infrastructure.persistence.entity.RelacionamentoFamiliarEntity;
import br.com.nsfatima.gestao.clero.domain.model.TipoPessoaFamiliar;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelacionamentoFamiliarJpaRepository extends JpaRepository<RelacionamentoFamiliarEntity, UUID> {
    List<RelacionamentoFamiliarEntity> findByPessoaOrigemIdAndTipoPessoaOrigemAndDeletedAtIsNull(
            UUID pessoaId, TipoPessoaFamiliar tipoPessoa);
    List<RelacionamentoFamiliarEntity> findByParIdAndDeletedAtIsNull(UUID parId);
}
